package com.cassava.core;

/**
 * Created by yan.dai on 4/11/2015.
 */
public interface IKeyspaceManager {
    boolean createKeyspace(String keyspace);
    boolean dropKeyspace(String keyspace);
    boolean hasKeyspace(String keyspace);
}
