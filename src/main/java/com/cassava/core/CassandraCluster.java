package com.cassava.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cassava.util.PropertiesUtil;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;

import java.util.*;

/**
 * Created by yan.dai on 4/11/2015.
 */
public class CassandraCluster implements IKeyspaceManager {
    private static Logger logger = LoggerFactory.getLogger(CassandraCluster.class);

    private static Map<String, CassandraCluster> cassandraClusters;

    private String keyspace;
    private Set<String> existingColumnFamilies; // a cache to store all the table's name

    // cassandra settings
    private String seedAddress;
    private int replicationFactor;
    private String[] nodeAddresses;
    private String[] dataDisks;
    private String cassandraConfigPath; // the path to cassandra property file


    // cassandra connection
    private Cluster cluster;
    private Session session;



    private CassandraCluster(String keyspace, String cassandraConfigPath) {
        this.existingColumnFamilies = new HashSet<>();
        this.keyspace = keyspace;
        this.cassandraConfigPath = cassandraConfigPath;
        setup(cassandraConfigPath);
    }

    public static CassandraCluster get(String keyspace) {
        return get(keyspace, "cassandra-cluster.properties");
    }

    public static CassandraCluster get(String keyspace, String cassandraConfigPath) {
        if(cassandraClusters == null) {
            synchronized (CassandraCluster.class) {
                if (cassandraClusters == null) {
                    cassandraClusters = new HashMap<>();
                }
            }
        }

        if(cassandraClusters != null && !cassandraClusters.containsKey(keyspace)) {
            synchronized (CassandraCluster.class) {
                if (cassandraClusters != null && !cassandraClusters.containsKey(keyspace))
                {
                    CassandraCluster cassandraCluster = new CassandraCluster(keyspace, cassandraConfigPath);
                    cassandraClusters.put(keyspace, cassandraCluster);
                }
            }
        }
        return cassandraClusters.get(keyspace);
    }

    private void setup(String cassandraConfigPath) {
        Properties cassandraProperties = PropertiesUtil.get().getProperties(cassandraConfigPath);
        if (cassandraProperties != null) {
            this.seedAddress = cassandraProperties.getProperty("seedaddress");
            this.replicationFactor = Integer.parseInt(cassandraProperties.getProperty("replicationfactor"));

            String nodeAddressesProp = cassandraProperties.getProperty("nodeAddresses");
            if(nodeAddressesProp != null) {
                this.nodeAddresses = nodeAddressesProp.split(",");
            }
            String dataDisksProp = cassandraProperties.getProperty("dataDisks");
            if(dataDisksProp != null) {
                this.dataDisks = dataDisksProp.split(",");
            }
            setupCassandraSession(seedAddress);
            createKeyspace(keyspace);
            setupColumnFamilyCache();
        } else {
            logger.error("cassandra properties file is required");
        }
    }


    private void setupCassandraSession(String seedAddress) {
        // setup cassandra connection
        Cluster.Builder builder = Cluster.builder();
        builder.addContactPoint(seedAddress);

        // set token aware policy
        builder.withLoadBalancingPolicy(new TokenAwarePolicy(new DCAwareRoundRobinPolicy()));

        // set pooling options core connections to max connections
        PoolingOptions options = new PoolingOptions();
        options.setCoreConnectionsPerHost(HostDistance.LOCAL, options.getMaxConnectionsPerHost(HostDistance.LOCAL));
        builder.withPoolingOptions(options);

        cluster = builder.build();

        // get a session for executing CQL statements
        session = cluster.connect();
    }

    @Override
    public boolean createKeyspace(String keyspace) {
        if(session != null) {
            String exec = "CREATE KEYSPACE IF NOT EXISTS " + keyspace +
                    " WITH replication = {'class':'SimpleStrategy', 'replication_factor':" + replicationFactor + "};";
            session.execute(exec);
            return true;
        }
        logger.warn("Keyspace creation failed, there is no available session cassandra node: " + seedAddress);
        return false;
    }

    @Override
    public boolean dropKeyspace(String keyspace)
    {
        if(session != null) {
            String exec = "DROP KEYSPACE IF EXISTS " + keyspace + ";";
            session.execute(exec);
            return true;
        }
        logger.warn("Keyspace deletion failed, there is no available session cassandra node: " + seedAddress);
        return false;
    }

    @Override
    public boolean hasKeyspace(String keyspace)
    {
        if(cassandraClusters == null) {
            logger.warn("Keyspace deletion failed, there is no available session cassandra node: " + seedAddress);
            return false;
        }
        return cassandraClusters.containsKey(keyspace);
    }

    // return true if table exists
    public boolean hasColumnFamilyExists( String tableName ) {
        return hasColumnFamilyExists( tableName, false, false );
    }


    public boolean hasColumnFamilyExists( String columnFamily, boolean forceCheck, boolean retry ) {
        // return true if the table specified exists in Cassandra
        if (!forceCheck && existingColumnFamilies != null && columnFamily != null && existingColumnFamilies.contains(columnFamily))
        {
            return true;
        }

        if ( session == null || keyspace == null || columnFamily == null )
        {
            return false;
        }

        StringBuilder sb = new StringBuilder();

        // select from system table to find if table exists
        sb.append("select columnfamily_name from system.schema_columnfamilies ");
        sb.append("where keyspace_name='").append(keyspace).append("' and ");
        sb.append("columnfamily_name='").append(columnFamily).append("';");
        String stmt = sb.toString();
        ResultSet results = session.execute( stmt );
        boolean tableExists = (results.one() != null);

        if (!tableExists && retry)
        {
            int failCount = 0;
            while (!tableExists && failCount < 3)
            {
                // sleep for 4 seconds
                try { Thread.sleep(4000); } catch (InterruptedException e) { /* ignore */ }
                results = session.execute( stmt );
                tableExists = (results.one() != null);
                if(!tableExists) failCount++;
            }
        }

        if (tableExists) { existingColumnFamilies.add(columnFamily); }
        return tableExists;
    }

    // close the cassandra connection on exit
    public void close()
    {
        if ( session != null )
        {
            session.close();
        }
        if ( cluster != null )
        {
            cluster.close();
        }
    }

    private void setupColumnFamilyCache() {
        List<String> existingCFs = getColumnFamilyNameList();
        for(String cf : existingCFs) {
            this.existingColumnFamilies.add(cf);
        }
    }

    // list all items/column families in a keyspace - and return their names as a list of strings
    public List<String> getColumnFamilyNameList()
    {
        List<String> columnFamilyList = new ArrayList<>();

        ResultSet resultSet = session
                .execute("SELECT columnfamily_name FROM system.schema_columnfamilies WHERE keyspace_name='" + this.keyspace + "';");
        if ( resultSet == null ) { return columnFamilyList; } // return an empty list
        for (Row row : resultSet)
        {
            String str = row.getString(0);
            if (str != null) columnFamilyList.add(str);
        }
        return columnFamilyList;
    }

    // getters
    public synchronized Session getSession()
    {
        return session;
    }

    public String getKeyspace()
    {
        return keyspace;
    }

    public Set<String> getExistingColumnFamilies()
    {
        return existingColumnFamilies;
    }

    public String getSeedAddress()
    {
        return seedAddress;
    }

    public int getReplicationFactor()
    {
        return replicationFactor;
    }

    public String[] getNodeAddresses()
    {
        return nodeAddresses;
    }

    public String[] getDataDisks()
    {
        return dataDisks;
    }

    public String getCassandraConfigPath()
    {
        return cassandraConfigPath;
    }

    public Cluster getCluster()
    {
        return cluster;
    }
}
