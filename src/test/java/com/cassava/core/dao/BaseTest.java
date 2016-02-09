package com.cassava.core.dao;

import com.cassava.core.CassandraCluster;
import com.cassava.core.TestUtil;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import org.junit.Before;

/**
 * Created by yan.dai on 12/11/2015.
 */
public abstract class BaseTest {

    protected final static String keyspace = "mykeyspace";
    protected static CassandraCluster cluster;
    protected static Session session;

    @Before
    public void init() {
        cluster = CassandraCluster.get(keyspace, TestUtil.get().getFilePath("cassandra-cluster.properties"));
        try {
            session = cluster.getCluster().connect(keyspace);
        } catch (InvalidQueryException e) {
            e.printStackTrace();
        }
    }

}
