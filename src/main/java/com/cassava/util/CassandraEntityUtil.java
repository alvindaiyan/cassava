package com.cassava.util;

/**
 * Created by yan.dai on 4/11/2015.
 */
public class CassandraEntityUtil {

    private static CassandraEntityUtil INSTANCE = null;

    private CassandraEntityUtil (){}

    public static CassandraEntityUtil get() {
        if(INSTANCE == null) {
            INSTANCE = new CassandraEntityUtil();
        }
        return INSTANCE;
    }

    // turn a class type into a cassandra entity name
    public <T> String typeToEntityName( Class<T> type )
    {
        // get the name from the type
        String tempStr = type.toString().trim();
        int i = tempStr.lastIndexOf('.');
        if ( i > 0 ) tempStr = tempStr.substring(i + 1).trim();
        return tempStr.toLowerCase();
    }





}
