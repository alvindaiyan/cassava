//package com.cassava.core.dao.enumtest;
//
//import com.cassava.core.dao.BaseTest;
//import com.cassava.core.dao.basetest.MyEnumType;
//import com.cassava.util.UUIDUtil;
//import com.google.gson.Gson;
//import org.junit.Test;
//
//import java.util.UUID;
//
//import static org.junit.Assert.assertEquals;
//
///**
// * Created by yan.dai on 12/11/2015.
// */
//public class EnumTest extends BaseTest
//{
//
//    @Test
//    public void testEnum() {
//        EnumExampleDAO dao = EnumExampleDAO.get(keyspace);
//
//        EnumExample example = new EnumExample();
//        UUID id = UUIDUtil.generateUUIDByTime();
//        example.setId(id);
//        example.setState("my state 2");
//        example.setDescription("this is the description");
//        example.setEnumType(MyEnumType.A);
//
//        Gson gson = new Gson();
//        System.out.println(gson.toJson(example));
//        dao.insert(example);
//
////        EnumExample myExample = dao.selectRow(id);
////        assertEquals(myExample.getEnumType(), MyEnumType.A);
//
//        dao.delete(id);
//    }
//}
