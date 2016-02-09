package com.cassava.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by yan.dai on 4/11/2015.
 */
public class PropertiesUtil {
    private static PropertiesUtil INSTANCE = null;
    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);


    public static PropertiesUtil get() {
        if (INSTANCE == null) {
            INSTANCE = new PropertiesUtil();
        }
        return INSTANCE;
    }

    public Properties getProperties(String path) {
        try {
            FileInputStream stream = new FileInputStream(path);
            Properties properties = new Properties();
            properties.load(stream);
            return properties;
        } catch (IOException e) {
            logger.error("load cassandra property error", e);
            return null;
        }
    }
}
