package com.cassava.util;

import com.cassava.core.model.IDataTransferObject;

import java.util.*;

/**
 * Created by yan.dai on 5/11/2015.
 */
public class ColumnFamilyCreatorUtil {
    private static ColumnFamilyCreatorUtil singleton = null;
    private ColumnFamilyCreatorUtil(){}


    public static ColumnFamilyCreatorUtil get() {
        if (singleton == null) {
            singleton = new ColumnFamilyCreatorUtil();
        }
        return singleton;
    }

    public String getCreateSecondaryIndexQuery(IDataTransferObject.CF cf, String tableName, String keyspace) {
        StringBuilder query = new StringBuilder();

        for(Map.Entry<String, String> entry : cf.getIndexes().entrySet()) {
            query.append(" CREATE INDEX IF NOT EXISTS ");
            query.append(entry.getKey())
                    .append(" ON ")
                    .append(keyspace)
                    .append(".")
                    .append(tableName)
                    .append(" (")
                    .append(entry.getValue())
                    .append(");");
        }
        return query.toString();
    }


    public String getCreateQuery(String tableName, IDataTransferObject.CF cf, String keyspace) {
        return getCreateQuery(tableName, cf, keyspace, false, true, false);
    }


    /**
     * When to compress data:
     * Compression is best suited for tables that have many rows and each row has the same columns,
     * or at least as many columns, as other rows. For example, a table containing user data
     * such as username, email, and state, is a good candidate for compression.
     * The greater the similarity of the data across rows, the greater the compression ratio and gain in read performance.
     *
     * A table that has rows of different sets of columns is not well-suited for compression.
     * Dynamic tables do not yield good compression ratios.
     *
     * Don't confuse table compression with compact storage of columns,
     * which is used for backward compatibility of old applications with CQL.
     *
     * Depending on the data characteristics of the table, compressing its data can result in:
     *
     * 2x-4x reduction in data size
     * 25-35% performance improvement on reads
     * 5-10% performance improvement on writes
     * After configuring compression on an existing table, subsequently created SSTables
     * are compressed. Existing SSTables on disk are not compressed immediately.
     * Cassandra compresses existing SSTables when the normal Cassandra compaction process occurs.
     * Force existing SSTables to be rewritten and compressed by using nodetool upgradesstables (Cassandra 1.0.4 or later) or nodetool scrub.
     *
     *
     * Key Cache:
     * The key cache holds the location of keys in memory on a per-column family basis.
     * For column family level read optimizations, turning this value up can have an immediate impact as soon as the cache warms.
     * Key caching is enabled by default, at a level of 200,000 keys.
     *
     * Row Cache:
     * Unlike the key cache, the row cache holds the entire contents of the row in memory.
     * It is best used when you have a small subset of data to keep hot and you frequently need most or all of the columns returned.
     * For these use cases, row cache can have substantial performance benefits.
     *
     * When to use Leveled Compaction: http://www.datastax.com/dev/blog/when-to-use-leveled-compaction
     *
     */
    public String getCreateQuery(String columnFamily, IDataTransferObject.CF cf, String keyspace, boolean useCompression, boolean useKeyCachingOnly, boolean useLeveledCompaction) {


        StringBuilder query = new StringBuilder();
        query.append("CREATE TABLE IF NOT EXISTS ")     // table is used in cql and column family is used in thrift
                .append(keyspace)
                .append(".")
                .append(columnFamily)
                .append(" (");
        for(Map.Entry<String, Class<?>> col : cf.getColumns().entrySet()) {
            query.append(col.getKey())
                    .append(" ")
                    .append(getCassandraType(col.getValue()))
                    .append(", ");
        }
        if (cf.getKeys().size() == 0) query.setLength(query.length() - 2);

        // set primary keys;
        query.append("PRIMARY KEY (");

        Set<String> keys = new HashSet<>();

        if(cf.getMachineKeys() != null && cf.getMachineKeys().size() > 0) {
            query.append("(");
            for(Map.Entry<String, Class<?>> machineKey : cf.getMachineKeys().entrySet()) {
                query.append(machineKey.getKey()).append(", ");
                keys.add(machineKey.getKey());
            }
            query.setLength( query.length() - 2);
            query.append("), ");
        }
        for (Map.Entry<String, Class<?>> key : cf.getKeys().entrySet()) {
            if (!keys.contains(key.getKey())) {
                query.append(key.getKey()).append(", ");
            }
        }
        query.setLength(query.length() - 2);

        query.append(")"); // end of primary keys

        query.append(")"); // end of query part

        // start of the property
        if ( useCompression )
        {
            query.append( " WITH compression = { 'sstable_compression' : 'LZ4Compressor' }" );
        }
        else
        {
            query.append( " WITH compression = { 'sstable_compression' : '' }" );
        }

        if ( useKeyCachingOnly )
        {
            query.append( " AND caching = 'keys_only'"  ); // default option
        }
        else
        {
            query.append( " AND caching = 'all'"); // this will improving reading performance but eat alot of memory
        }

        if ( useLeveledCompaction )
        {
            query.append( " AND compaction = {'class':'LeveledCompactionStrategy'}" );
        }

        query.append(";");

        return query.toString();
    }

    // http://docs.datastax.com/en/developer/java-driver/1.0/java-driver/reference/javaClass2Cql3Datatypes_r.html
    // todo: to be improved as there are redundant
    private String getCassandraType(Class<?> cls) {

        if (cls.isAssignableFrom(long.class) || cls.isAssignableFrom(Long.class))
        {
            return "bigint";
        }
        else if (cls.isAssignableFrom(java.nio.ByteBuffer.class))
        {
            return "blob";
        }
        else if (cls.isAssignableFrom(boolean.class) || cls.isAssignableFrom(Boolean.class))
        {
            return "boolean";
        }
        else if (cls.isAssignableFrom(long.class) || cls.isAssignableFrom(Long.class))
        {
            return "counter";
        }
        else if (cls.isAssignableFrom(java.math.BigDecimal.class))
        {
            return "decimal";
        }
        else if (cls.isAssignableFrom(double.class) || cls.isAssignableFrom(Double.class))
        {
            return "double";
        }
        else if (cls.isAssignableFrom(float.class) || cls.isAssignableFrom(Float.class))
        {
            return "float";
        }
        else if (cls.isAssignableFrom(java.net.InetAddress.class))
        {
            return "inet";
        }
        else if (cls.isAssignableFrom(int.class) || cls.isAssignableFrom(Integer.class))
        {
            return "int";
        }
//        else if (cls.isAssignableFrom(java.util.List.class))
//        {
//            Field[] fields = cls.getDeclaredFields();
//            for(Field field : fields) {
//                System.out.println(field.getGenericType());
//            }
//
////            String genericTypeStr = getCassandraType(genericType);
////            return "list<" + genericTypeStr + ">";
//            return "list";
//        }
//        else if (cls.isAssignableFrom(java.util.Map.class))
//        {
//            return "map";
//        }
//        else if (cls.isAssignableFrom(java.util.Set.class))
//        {
//            return "set";
//        }
        else if (cls.isAssignableFrom(java.lang.String.class))
        {
            return "text";
        }
        else if (cls.isAssignableFrom(java.util.UUID.class))
        {
            return "uuid";
        }
        else if (cls.isAssignableFrom(java.lang.String.class))
        {
            return "varchar";
        }
        else if (cls.isAssignableFrom(java.math.BigInteger.class))
        {
            return "varint";
        }
        else if (cls.isAssignableFrom(java.util.Date.class))
        {
            return "timestamp";
        }
        else if (cls.isAssignableFrom(java.util.UUID.class))
        {
            return "timeuuid";
        }
        else
        {
//            throw new IllegalArgumentException("unsupported Java Type: " + cls.getName());
            return "text"; // treat as a json string
        }
    }

    public boolean isCassandraPrimitiveType(Class<?> cls) {
        if (cls.isAssignableFrom(long.class) || cls.isAssignableFrom(Long.class))
        {
            return true;
        }
        else if (cls.isAssignableFrom(java.nio.ByteBuffer.class))
        {
            return true;
        }
        else if (cls.isAssignableFrom(boolean.class) || cls.isAssignableFrom(Boolean.class))
        {
            return true;
        }
        else if (cls.isAssignableFrom(long.class) || cls.isAssignableFrom(Long.class))
        {
            return true;
        }
        else if (cls.isAssignableFrom(java.math.BigDecimal.class))
        {
            return true;
        }
        else if (cls.isAssignableFrom(double.class) || cls.isAssignableFrom(Double.class))
        {
            return true;
        }
        else if (cls.isAssignableFrom(float.class) || cls.isAssignableFrom(Float.class))
        {
            return true;
        }
        else if (cls.isAssignableFrom(java.net.InetAddress.class))
        {
            return true;
        }
        else if (cls.isAssignableFrom(int.class) || cls.isAssignableFrom(Integer.class))
        {
            return true;
        }
//        else if (cls.isAssignableFrom(java.util.List.class))
//        {
//            return true;
//        }
//        else if (cls.isAssignableFrom(java.util.Map.class))
//        {
//            return true;
//        }
//        else if (cls.isAssignableFrom(java.util.Set.class))
//        {
//            return true;
//        }
        else if (cls.isAssignableFrom(java.lang.String.class))
        {
            return true;
        }
        else if (cls.isAssignableFrom(java.util.UUID.class))
        {
            return true;
        }
        else if (cls.isAssignableFrom(java.lang.String.class))
        {
            return true;
        }
        else if (cls.isAssignableFrom(java.math.BigInteger.class))
        {
            return true;
        }
        else if (cls.isAssignableFrom(java.util.Date.class))
        {
            return true;
        }
        else if (cls.isAssignableFrom(java.util.UUID.class))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
