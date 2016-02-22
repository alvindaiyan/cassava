//package com.cassava.core.dao.createtest;
//
//import com.cassava.core.dao.BaseDAO;
//
///**
// * Created by yan.dai on 12/11/2015.
// */
//public class CreationExampleDAO extends BaseDAO<CreationExample> {
//    private static CreationExampleDAO INSTANCE = null;
//
//    protected CreationExampleDAO(Class<CreationExample> type, String keyspace) {
//        super(type, keyspace);
//    }
//
//    public static CreationExampleDAO get(String keyspace ) {
//        if(INSTANCE == null) {
//            INSTANCE = new CreationExampleDAO(CreationExample.class, keyspace);
//        }
//        return INSTANCE;
//    }
//}
