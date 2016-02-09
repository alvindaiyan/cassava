package com.cassava.core.dao;

import com.cassava.core.CassandraCluster;
import com.cassava.core.model.IBaseDAO;
import com.cassava.core.model.IDataTransferObject;
import com.cassava.reflection.AnnotationRunner;
import com.cassava.util.ColumnFamilyCreatorUtil;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by yan.dai on 4/11/2015.
 */
public abstract class BaseDAO<K, V extends IDataTransferObject> extends CacheLoader<K, V> implements IBaseDAO<V> {
    // static fields
    private static Logger logger = LoggerFactory.getLogger(BaseDAO.class);
    private final static long DEFAULT_CACHE_DURATION = 1;
    private final static TimeUnit DEFAULT_CACHE_TIMEUNIT = TimeUnit.HOURS;

    public final static int MAX_CACHE_SIZE = 1000000;

    private final static int ROW_COUNT_LIMIT = 1000;
    private final static int RETRY_DELAY = 4000; // 4 seconds
    private final static int RETRY_COUNT = 3; // return 3 times

    private Class<V> type;
    private String keyspace;


    protected String tableName;
    protected IDataTransferObject.CF cf;
    protected Map<String, Class<?>> userDefinedColumn; // columns whose data type is not a standard cassandra type, but a user defined type


    // thread safe gson instance
    protected Gson gson;

    // cache
    // https://github.com/google/guava/wiki/CachesExplained
    protected LoadingCache<K, V> cache;
    private long cacheDuration;
    private TimeUnit cacheTimeUnit;


    protected BaseDAO( Class<V> type, String keyspace, long cacheDuration, TimeUnit cacheTimeUnit) {
        this.type = type;
        // if no keyspace use default keyspace
        this.keyspace = keyspace;

        // if no table name then get table name from type of dto object
        this.tableName = AnnotationRunner.get().getColumnFamilyNameFromAnnotation(type);
        this.cf = AnnotationRunner.get().getColumnNamesFromAnnotation(type);
        this.userDefinedColumn = getUserDefinedColumn(this.cf.getColumns());
        // setup an instance based (is thread safe) gson object
        this.gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .disableHtmlEscaping()
                .create();

        createColumnFamily();
        createSecondaryIndexes();

        this.cacheDuration = cacheDuration;
        this.cacheTimeUnit = cacheTimeUnit;
    }


    protected BaseDAO( Class<V> type, String keyspace) {
        this(type, keyspace, DEFAULT_CACHE_DURATION, DEFAULT_CACHE_TIMEUNIT);
    }

    // todo:  query used without partition key?
    @Override
    public int rowCount() {
        Select select = QueryBuilder.select()
                .countAll()
                .from(keyspace, this.tableName)
                .limit(ROW_COUNT_LIMIT);
        ResultSet resultSet = execute(select);
        if ( resultSet != null ) {
            Row row = resultSet.one();
            if ( row != null ) return (int)row.getLong(0);
        }
        return 0;
    }


    @Override
    public V selectRow(UUID offset)
    {
        List<V> selects = selectRows(offset, 1);
        if(selects == null || selects.size() == 0) {
            return null;
        }
        return selects.get(0);
    }

    @Override
    public List<V> selectRows(UUID offset, int limit) {
        String select = getSelectQuery(offset, limit, limit > 1);

        logger.debug("select query: " + select);
        ResultSet result = CassandraCluster.get(keyspace).getSession().execute(select);

        if  ( result != null ) {
            List<V> objectList = new ArrayList<>();
            for (Row row : result) {
                String str = reformatResult(row.getString(0));
                logger.debug("decode string for select rows: " + str);

                if (str != null) {
                    try {
                        V obj = gson.fromJson(str, type);
                        if (obj != null)  {
                            objectList.add(obj);
                        }
                    } catch (JsonSyntaxException e) {
                        logger.error("error parsing json: " + str + ", for class " + type.toString(), e);
                    }
                }
            }
            return objectList;
        }
        return null;
    }


    private String getSelectQuery(UUID offset, int limit, boolean single) {
        String select = "SELECT JSON * FROM "+ keyspace + "." + tableName;

        if(offset != null ) {
            select += " WHERE token(id)" + (single? ">=" : "=") + " token(" + offset + ")";
        }
        if(limit > 0) {
            select +=   " LIMIT " + limit;
        }
        select += ";";
        return  select;
    }


    @Override
    public void insert(V obj) {
        Insert insert = QueryBuilder
                .insertInto(keyspace, tableName);

        for (Map.Entry<String, Class<?>> column : cf.getColumns().entrySet()) {
            try {
                Object value = AnnotationRunner.get().getFieldValue(obj, column.getKey());

                if(value == null) {
                    throw new NullPointerException("no value for field: " + column.getKey() + ", and suspend insert");
                }

                if(ColumnFamilyCreatorUtil.get().isCassandraPrimitiveType(value.getClass())) {
                    insert.value(column.getKey(), value);
                } else {
                    insert.value(column.getKey(), gson.toJson(value));
                }

            } catch (NoSuchFieldException | IllegalAccessException e) {
                logger.error("cannot get value of " + column.getKey(), e);
            }
        }
        logger.debug(insert.toString());
        execute(insert);
    }

    @Override
    public void delete(UUID offset) {
        if (offset == null) {
            return;
        }

        Delete.Where delete = QueryBuilder
                .delete()
                .from(keyspace, tableName)
                .where(QueryBuilder.eq("id", offset));
        logger.debug(delete.toString());
        execute(delete);
    }

    @Override
    public ResultSet execute(Statement statement) {
        int failCount = 0;
        Exception exception = null;
        int delay = RETRY_DELAY;
        while (failCount < RETRY_COUNT)
        {
            try
            {
                return CassandraCluster.get(keyspace).getSession().execute(statement);
            }
            catch (Exception e)
            {
                exception = e;
                logger.warn("error (" + e.getMessage() + ") executing cassandra query : " + statement);
                try
                {
                    Thread.sleep( delay );
                    delay = delay * 2;
                    logger.info("retrying cassandra query");
                } catch (InterruptedException ie) { /* ignore */ }
            }
            failCount++;
        }
        logger.error("error cassandra query " + failCount + " times. aborting", exception);
        return null;
    }



    // cache functions
    @Override
    public V load(K key) throws Exception {
        throw new UnsupportedOperationException("you need provide your own loading function");
    }

    public LoadingCache<K, V> getCache() {
        if(cache == null) {
            setupCache();
        }
        return cache;
    }

    public synchronized void setupCache() {
        if (cache == null) {
            this.cache = CacheBuilder
                    .newBuilder()
                    .maximumSize(MAX_CACHE_SIZE)
                    .expireAfterAccess(this.cacheDuration, this.cacheTimeUnit) // expire after a day of access
                    .build(this);
        }
    }

    private String reformatResult(String original) {
        if(userDefinedColumn != null && userDefinedColumn.size() > 0) {
            JsonObject raw = new JsonParser().parse(original).getAsJsonObject();
            for(Map.Entry<String, Class<?>> entry : userDefinedColumn.entrySet()) {
                if (raw.has(entry.getKey().toLowerCase())) {
                    // firstly, decode to a String object;
                    String jsonStr = gson.fromJson(raw.get(entry.getKey().toLowerCase()), String.class);
                    JsonElement obj = gson.fromJson(jsonStr, JsonElement.class);
                    // now update the original object to proper format
                    raw.add(entry.getKey(), obj);
                }
            }
            return gson.toJson(raw);
        }
        return original;
    }


    private Map<String, Class<?>> getUserDefinedColumn( Map<String, Class<?>> columns) {
        if (columns == null) { return new HashMap<>(); }
        Map<String, Class<?>> result = new HashMap<>();
        for(Map.Entry<String, Class<?>> entry : columns.entrySet()) {
            if(!ColumnFamilyCreatorUtil.get().isCassandraPrimitiveType(entry.getValue())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }


    // check if column family dose not exist
    // if not, create the column family as json column file
    private void createColumnFamily() {
        logger.debug("start create column family: " + this.keyspace + "." + this.tableName);
        // cluster
        CassandraCluster cluster = CassandraCluster.get(this.keyspace);

        if ( cluster != null  ) {
            if (cluster.hasColumnFamilyExists(this.tableName)) {
                logger.debug("table already exists");
                return;
            }

            String creationQuery = ColumnFamilyCreatorUtil.get().getCreateQuery(this.tableName, this.cf, keyspace);
            logger.debug("get the creation query: " + creationQuery);

            cluster.getSession().execute(creationQuery);
        } else {
            throw new IllegalStateException("cassandra cluster required");
        }
    }


    // create the secondary indexes for a table
    private void createSecondaryIndexes() {
        logger.debug("start create column family: " + this.keyspace + "." + this.tableName);
        // cluster
        CassandraCluster cluster = CassandraCluster.get(this.keyspace);

        if ( cluster != null  ) {
            String creationQuery = ColumnFamilyCreatorUtil.get().getCreateSecondaryIndexQuery(this.cf, this.tableName,  keyspace);
            logger.debug("get the creation query: " + creationQuery);

            cluster.getSession().execute(creationQuery);
        } else {
            throw new IllegalStateException("cassandra cluster required");
        }
    }


    public IDataTransferObject.CF getCf() {
        return cf;
    }

    public String getTableName() {
        return tableName;
    }
}
