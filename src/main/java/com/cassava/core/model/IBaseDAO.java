package com.cassava.core.model;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Statement;

import java.util.List;
import java.util.UUID;

/**
 * Created by yan.dai on 6/11/2015.
 */
public interface IBaseDAO<T extends IDataTransferObject> {
    T selectRow(UUID offset);
    List<T> selectRows(UUID offset, int limit);
    void insert(T obj);
    void delete(UUID offset);
    int rowCount();
    ResultSet execute(Statement statement);
}
