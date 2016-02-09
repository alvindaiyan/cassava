package com.cassava.core.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by yan.dai on 6/11/2015.
 */
public interface IDataTransferObject {
    UUID getId();


    class CF {
        private Map<String, Class<?>> keys;
        private Map<String, Class<?>> columns;
//        private Map<String, Class<?>> genericTypesMapper; // column name -> type
        private Map<String, Class<?>> machineKeys;
        private Map<String, String> indexes;

        public CF() {
            this.keys = new HashMap<>();
            this.columns = new HashMap<>();
            this.machineKeys = new HashMap<>();
//            this.genericTypesMapper = new HashMap<>();
            this.indexes = new HashMap<>();
        }

        public Map<String, Class<?>> getKeys()
        {
            return keys;
        }

        public Map<String, Class<?>> getColumns()
        {
            return columns;
        }

        public Map<String, Class<?>> getMachineKeys() {
            return machineKeys;
        }

        public Map<String, String> getIndexes() {
            return indexes;
        }

        //        public Map<String, Class<?>> getGenericTypesMapper()
//        {
//            return genericTypesMapper;
//        }

        @Override
        public String toString() {
            String str = "keys: ";

            for(Map.Entry<String, Class<?>> entry : keys.entrySet()) {
                str += entry.getKey() + " ";
            }

            str += " | columns: ";
            for(Map.Entry<String, Class<?>> entry : columns.entrySet()) {
                str += entry.getKey() + " ";
            }
            return str;
        }
    }
}
