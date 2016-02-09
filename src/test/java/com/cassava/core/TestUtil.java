package com.cassava.core;

import java.io.File;

/**
 * Created by daiyan on 9/11/15.
 */
public class TestUtil {

    private static TestUtil singleton = null;

    private TestUtil() {
    }

    public static TestUtil get() {
        if (singleton == null) {
            singleton = new TestUtil();
        }
        return singleton;
    }

    public String getFilePath(String fname) {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fname).getFile());
        return file.getAbsolutePath();
    }
}
