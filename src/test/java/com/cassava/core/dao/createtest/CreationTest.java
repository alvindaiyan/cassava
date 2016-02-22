//package com.cassava.core.dao.createtest;
//
//import com.cassava.core.dao.BaseTest;
//import com.cassava.util.ColumnFamilyCreatorUtil;
//import com.datastax.driver.core.ResultSet;
//import com.datastax.driver.core.Row;
//import org.junit.Test;
//
//import static org.junit.Assert.assertTrue;
//
///**
// * Created by yan.dai on 12/11/2015.
// */
//public class CreationTest extends BaseTest {
//
//    @Test
//    public void testCreate() {
//        CreationExampleDAO dao = CreationExampleDAO.get(keyspace);
//        System.out.println(ColumnFamilyCreatorUtil.get().getCreateQuery(dao.getTableName(), dao.getCf(), keyspace));
//        System.out.println("select columnfamily_name from system.schema_columnfamilies where keyspace_name='" + keyspace + "' and columnfamily_name = '" + dao.getTableName() + "';");
//
//        // test create
//        ResultSet result = session.execute("select columnfamily_name from system.schema_columnfamilies " +
//                "where keyspace_name='" + keyspace + "' and columnfamily_name = '" + dao.getTableName() + "';");
//        for (Row row : result) {
//            assertTrue(row.getString("columnfamily_name").equals(dao.getTableName()));
//        }
//        System.out.println("get table name: " + result);
//    }
//}
