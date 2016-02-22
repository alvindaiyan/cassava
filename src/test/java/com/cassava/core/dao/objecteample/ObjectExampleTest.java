//package com.cassava.core.dao.objecteample;
//
//import com.cassava.core.dao.BaseTest;
//import com.cassava.core.dao.basetest.Example;
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
//public class ObjectExampleTest extends BaseTest {
//
//    @Test
//    public void testCreate() {
//        ObjectExampleDAO dao = ObjectExampleDAO.get(keyspace);
//
//        ObjectExample example = new ObjectExample();
//        UUID id = UUIDUtil.generateUUIDByTime();
//        example.setId(id);
//        example.setState("my state 2");
//        example.setDescription("this is the description");
//
//        HelperExample helperExample = new HelperExample();
//        helperExample.setField1("field 1");
//        helperExample.setField2("field 2");
//        helperExample.setField3("field 3");
//
//        example.setExample(helperExample);
//
//        Gson gson = new Gson();
//        System.out.println(gson.toJson(example));
//
//        dao.insert(example);
//
//        ObjectExample myExample = dao.selectRow(id);
//        assertEquals(myExample.getExample().getField1(), "field 1");
//        assertEquals(myExample.getExample().getField2(), "field 2");
//        assertEquals(myExample.getExample().getField3(), "field 3");
//
//        dao.delete(id);
//    }
//
//}
