package com.cassava.core.dao.basetest;

import com.cassava.core.dao.BaseDAO;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Created by yan.dai on 5/11/2015.
 */
public class ExampleDAO extends BaseDAO<UUID, Example> {

    private static ExampleDAO INSTANCE = null;

    ExampleDAO(Class<Example> type, String keyspace)
    {
        super(type, keyspace);
    }

    public static ExampleDAO get(String keyspace) {
        if(INSTANCE == null) {
            synchronized (ExampleDAO.class) {
                if (INSTANCE == null) INSTANCE = new ExampleDAO(Example.class, keyspace);
            }
        }
        return INSTANCE;
    }


    public Example getFromCache(UUID key) throws ExecutionException {
        return this.cache.get(key);
    }

}
