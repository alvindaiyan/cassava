package com.cassava.reflection;

import com.cassava.annotations.Column;
import com.cassava.annotations.Key;
import com.cassava.annotations.PartitionKey;
import com.cassava.annotations.SecondaryIndex;
import com.cassava.core.model.IDataTransferObject;
import com.cassava.util.CassandraEntityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by yan.dai on 6/11/2015.
 */
public class AnnotationRunner {
    Logger logger = LoggerFactory.getLogger(AnnotationRunner.class);

    private static AnnotationRunner INSTANCE = null;

    private AnnotationRunner(){}

    public static AnnotationRunner get() {
        if(INSTANCE == null) {
            INSTANCE = new AnnotationRunner();
        }
        return INSTANCE;
    }


    public String getColumnFamilyNameFromAnnotation (Class<? extends IDataTransferObject> cls) {
        // handle the column-family name
        if(cls.isAnnotationPresent(com.cassava.annotations.ColumnFamily.class)) {
            Annotation annotation = cls.getAnnotation(com.cassava.annotations.ColumnFamily.class);
            com.cassava.annotations.ColumnFamily cf = (com.cassava.annotations.ColumnFamily) annotation;
            // use the value of the annotation
            if(cf.name() != null && cf.name().length() > 0) {
                return cf.name();
            }
            // use the class name
            return CassandraEntityUtil.get().typeToEntityName(cls);
        } else return null;
    }



    public IDataTransferObject.CF getColumnNamesFromAnnotation (Class<? extends IDataTransferObject> cls) {
        Field[] fields = cls.getDeclaredFields();
        logger.debug("class type: " + cls);
        IDataTransferObject.CF cf = new IDataTransferObject.CF();
        for(Field field : fields) {
            String columnName = field.getName();
            // deal with the column annotation
            if(field.isAnnotationPresent(Column.class)) {
                columnName = processColumn(field, cf);
            }

            // deal with the machine key annotation
            if(field.isAnnotationPresent(PartitionKey.class)) {
                columnName = processMachineKey(field, cf);
            }

            // deal with the key annotation
            if(field.isAnnotationPresent(Key.class)) {
                columnName = processKey(field, cf);
            }

            if(field.isAnnotationPresent(SecondaryIndex.class)) {
                processSecondaryIndexes(columnName, field, cf);
            }
        }
        return cf;
    }


    private void processSecondaryIndexes(String columnName, Field field, IDataTransferObject.CF cf) {
        logger.debug("field: " + field.getName() + " type: " + field.getType());
        SecondaryIndex secondaryIndex = field.getAnnotation(SecondaryIndex.class);
        cf.getIndexes().put(secondaryIndex.name(), columnName);
    }

    private String processMachineKey(Field field, IDataTransferObject.CF cf) {
        logger.debug("field: " + field.getName() + " type: " + field.getType());
        PartitionKey partitionKey = field.getAnnotation(PartitionKey.class);
        String fieldName = field.getName();
        if(partitionKey.name() != null && partitionKey.name().length() > 0) {
            fieldName = partitionKey.name();
        }
        cf.getMachineKeys().put(fieldName, field.getType());
        if(!cf.getColumns().containsKey(fieldName)) {
            cf.getColumns().put(fieldName, field.getType());
        }
        return fieldName;
    }


    private String processColumn(Field field, IDataTransferObject.CF cf) {
        logger.debug("field: " + field.getName() + " type: " + field.getType());
        Column column = field.getAnnotation(Column.class);
        String fieldName = field.getName();
        if(column.name() != null && column.name().length() > 0) {
            fieldName = column.name();
        }
        cf.getColumns().put(fieldName, field.getType());
        return fieldName;
    }


    private String processKey(Field field, IDataTransferObject.CF cf) {
        logger.debug("field: " + field.getName() + " type: " + field.getType());
        Key key = field.getAnnotation(Key.class);
        String fieldName = field.getName();
        if(key.name() != null && key.name().length() > 0) {
            fieldName = key.name();
        }
        cf.getKeys().put(fieldName, field.getType());
        if(!cf.getColumns().containsKey(fieldName)) {
            cf.getColumns().put(fieldName, field.getType());
        }
        logger.debug("field key: " + field.getName() + " type: " + field.getType());
        return fieldName;
    }

    private Class<?> getGenericType(Field field) {
        if(field == null) {
            return null;
        }
        if(hasGenericType(field.getType())) {
            ParameterizedType genericType = (ParameterizedType) field.getGenericType();
            return (Class<?>) genericType.getActualTypeArguments()[0];
        }
        return null;
    }

    private boolean hasGenericType(Class<?> cls) {
        return cls.isAssignableFrom(List.class) || cls.isAssignableFrom(Set.class) || cls.isAssignableFrom(Map.class);
    }


    public <T extends IDataTransferObject> Object getFieldValue(T obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(obj);
        field.setAccessible(false);
        return value;
    }


}
