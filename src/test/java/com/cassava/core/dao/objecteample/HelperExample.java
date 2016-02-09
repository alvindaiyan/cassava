package com.cassava.core.dao.objecteample;

import com.google.gson.annotations.Expose;

/**
 * Created by yan.dai on 9/11/2015.
 */
public class HelperExample {
    @Expose
    private String field1;
    @Expose
    private String field2;
    @Expose
    private String field3;

    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public String getField2() {
        return field2;
    }

    public void setField2(String field2) {
        this.field2 = field2;
    }

    public String getField3() {
        return field3;
    }

    public void setField3(String field3) {
        this.field3 = field3;
    }
}
