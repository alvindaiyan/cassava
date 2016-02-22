//package com.cassava.core.dao.enumtest;
//
//import com.cassava.core.dao.BaseDAO;
//
///**
// * Created by yan.dai on 12/11/2015.
// */
//public class EnumExampleDAO extends BaseDAO<EnumExample> {
//    private static EnumExampleDAO INSTANCE = null;
//
//    protected EnumExampleDAO(Class<EnumExample> type, String keyspace) {
//        super(type, keyspace);
//    }
//
//    public static EnumExampleDAO get(String keyspace) {
//        if(INSTANCE == null) {
//            synchronized (EnumExampleDAO.class) {
//                if (INSTANCE == null) INSTANCE = new EnumExampleDAO(EnumExample.class, keyspace);
//            }
//        }
//        return INSTANCE;
//    }
//
//}
