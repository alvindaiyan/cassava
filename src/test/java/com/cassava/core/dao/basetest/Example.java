package com.cassava.core.dao.basetest;

import com.cassava.annotations.*;
import com.cassava.core.dao.objecteample.HelperExample;
import com.cassava.core.model.IDataTransferObject;
import com.google.gson.annotations.Expose;

import java.util.List;
import java.util.UUID;

/**
 * Created by yan.dai on 5/11/2015.
 */
@ColumnFamily
public class Example implements IDataTransferObject {
    @PartitionKey
    @Expose
    private UUID id;
    @Column
    @SecondaryIndex(name = "state_index")
    @Expose
    private String state;
    @Column
    @Expose
    private String description;
    @Column
    @Expose
    private HelperExample example;
    @Column
    @Expose
    private List<HelperExample> examples;
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

    public HelperExample getExample() {
        return example;
    }

    public List<HelperExample> getExamples() {
        return examples;
    }

    public void setExamples(List<HelperExample> examples) {
        this.examples = examples;
    }

    public void setExample(HelperExample example)
    {
        this.example = example;
    }

    public MyEnumType getEnumType() {
        return enumType;
    }

    public void setEnumType(MyEnumType enumType) {
        this.enumType = enumType;
    }
}
