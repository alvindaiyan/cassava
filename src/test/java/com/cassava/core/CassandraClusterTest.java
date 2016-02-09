package com.cassava.core;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by yan.dai on 4/11/2015.
 */
public class CassandraClusterTest {

    @Test
    public void testCassandraClusterSetup() {
        String keyspace = "mytest";
        CassandraCluster cluster = CassandraCluster.get(keyspace, "C:\\Dev\\cassava\\src\\test\\resources\\cassandra-cluster.properties");
        try {
            cluster.getCluster().connect("test");
        } catch (InvalidQueryException e) {
            assertTrue(true);
        }
        cluster.dropKeyspace(keyspace);
    }

    @Test
    public void testCassandraClusterSetup2() {
        String keyspace = "mytest";
        CassandraCluster cluster = CassandraCluster.get(keyspace, "C:\\Dev\\cassava\\src\\test\\resources\\cassandra-cluster.properties");
        try {
            Session session = cluster.getCluster().connect(keyspace);
            assertTrue(cluster.hasKeyspace(keyspace));
            assertNotNull(session);
        } catch (InvalidQueryException e) {
            assertTrue(true);
        }
        cluster.dropKeyspace(keyspace);
        try {
            cluster.getCluster().connect(keyspace);
            assertFalse(cluster.hasKeyspace(keyspace));
        } catch (InvalidQueryException e) {
            assertTrue(true);
        }
    }


}