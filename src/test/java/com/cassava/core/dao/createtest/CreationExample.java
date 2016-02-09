package com.cassava.core.dao.createtest;

import com.cassava.annotations.Column;
import com.cassava.annotations.Key;
import com.cassava.core.model.IDataTransferObject;
import com.google.gson.annotations.Expose;

import java.util.UUID;

/**
 * Created by yan.dai on 12/11/2015.
 */
public class CreationExample implements IDataTransferObject {

    @Key
    @Column
    @Expose
    private UUID id;
    @Column
    @Expose
    private String state;
    @Column
    @Expose
    private String description;

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

}
