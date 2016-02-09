package com.cassava.core.dao.basetest;

import com.cassava.core.dao.BaseTest;
import com.cassava.core.dao.objecteample.HelperExample;
import com.cassava.util.ColumnFamilyCreatorUtil;
import com.cassava.util.UUIDUtil;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.gson.Gson;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Created by yan.dai on 5/11/2015.
 */
public class DAOTest extends BaseTest {

    @Test
    public void testInsert() {
        ExampleDAO dao = ExampleDAO.get(keyspace);
        System.out.println(ColumnFamilyCreatorUtil.get().getCreateQuery(dao.getTableName(), dao.getCf(), keyspace));
        System.out.println("select columnfamily_name from system.schema_columnfamilies where keyspace_name='" + keyspace + "' and columnfamily_name = '" + dao.getTableName() + "';");

        // test create
        ResultSet result = session.execute("select columnfamily_name from system.schema_columnfamilies " +
                "where keyspace_name='" + keyspace + "' and columnfamily_name = '" + dao.getTableName() + "';");
        for (Row row : result) {
            assertTrue(row.getString("columnfamily_name").equals(dao.getTableName()));
        }
        System.out.println(result);

        Example example = new Example();
        UUID id = UUIDUtil.generateUUIDByTime();
        example.setId(id);
        example.setState("my state 2");
        example.setDescription("this is the description");

        HelperExample helperExample = new HelperExample();
        helperExample.setField1("field 1");
        helperExample.setField2("field 2");
        helperExample.setField3("field 3");

        HelperExample helperExample0 = new HelperExample();
        helperExample0.setField1("field 1");
        helperExample0.setField2("field 2");
        helperExample0.setField3("field 3");

        HelperExample helperExample1 = new HelperExample();
        helperExample1.setField1("field 1");
        helperExample1.setField2("field 2");
        helperExample1.setField3("field 3");


        List<HelperExample> lists = new ArrayList<>();
        lists.add(helperExample0);
        lists.add(helperExample1);

        example.setEnumType(MyEnumType.A);
        example.setExamples(lists);

        example.setExample(helperExample);
        Gson gson = new Gson();
        System.out.println(gson.toJson(example));

        dao.insert(example);

        // test insert
        result = session.execute("select * from " + keyspace + "." + dao.getTableName() + ";");
        boolean scc = false;
        int count = 0;
        for (Row row : result) {
            if (row.getUUID("id").equals(id) &&
                    row.getString("state").equals("my state 2") &&
                    row.getString("description").equals("this is the description")) {
                scc = true;
            }
            count++;
        }
        assertTrue(scc);

        // test row count
        int rowCount = dao.rowCount();
        assertTrue(rowCount == count);

        // test select
        List<Example> examples = dao.selectRows(id, 10);
        Example ex = examples.get(0);
        assertEquals(ex.getId(), id);
        assertEquals(ex.getState(), "my state 2");
        assertEquals(ex.getDescription(), "this is the description");
        assertEquals(ex.getExample().getField1(), "field 1");
        assertEquals(ex.getExample().getField2(), "field 2");
        assertEquals(ex.getExample().getField3(), "field 3");

        Example myExample = dao.selectRow(id);
        assertEquals(myExample.getId(), id);
        assertEquals(myExample.getState(), "my state 2");
        assertEquals(myExample.getDescription(), "this is the description");
        assertEquals(myExample.getExample().getField1(), "field 1");
        assertEquals(myExample.getExample().getField2(), "field 2");
        assertEquals(myExample.getExample().getField3(), "field 3");
        assertEquals(myExample.getEnumType(), MyEnumType.A);

        dao.delete(id);
        Example r = dao.selectRow(id);
        if (r != null) {
            System.out.println(id + " ******** " + r.getId());
        }
        assertNull(r);
    }


//    @After
//    public void end() {
//        cluster.dropKeyspace(keyspace);
//    }
}

