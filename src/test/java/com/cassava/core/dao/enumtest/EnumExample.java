package com.cassava.core.dao.enumtest;

import com.cassava.annotations.Column;
import com.cassava.annotations.ColumnFamily;
import com.cassava.annotations.Key;
import com.google.gson.annotations.Expose;
import com.cassava.core.dao.basetest.MyEnumType;
import com.cassava.core.model.IDataTransferObject;
import java.util.UUID;

/**
 * Created by yan.dai on 12/11/2015.
 */
@ColumnFamily
public class EnumExample implements IDataTransferObject {
    @Key
    @Column
    @Expose
    private UUID id;
    @Key
    @Column
    @Expose
    private String state;
    @Column
    @Expose
    private String description;
    @Column
    @Expose
    private MyEnumType enumType;

    @Override
    public UUID getId()
    {
        return id;
    }

    public void setId(UUID id)
    {
        this.id = id;
    }

    public String getState()
    {
        return state;
    }

    public void setState(String state)
    {
        this.state = state;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public MyEnumType getEnumType() {
        return enumType;
    }

    public void setEnumType(MyEnumType enumType) {
        this.enumType = enumType;
    }
}
