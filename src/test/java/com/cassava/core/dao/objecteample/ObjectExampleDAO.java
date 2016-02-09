package com.cassava.core.dao.objecteample;

import com.cassava.core.dao.BaseDAO;

/**
 * Created by yan.dai on 12/11/2015.
 */
public class ObjectExampleDAO extends BaseDAO<ObjectExample> {
    private static ObjectExampleDAO INSTANCE = null;

    protected ObjectExampleDAO(Class<ObjectExample> type, String keyspace) {
        super(type, keyspace);
    }

    public static ObjectExampleDAO get(String keyspace) {
        if(INSTANCE == null) {
            synchronized (ObjectExampleDAO.class) {
                if (INSTANCE == null) INSTANCE = new ObjectExampleDAO(ObjectExample.class, keyspace);
            }
        }
        return INSTANCE;
    }
}
