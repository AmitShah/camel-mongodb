/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.component.mongodb.converters;

import java.util.Map;

import org.apache.camel.Converter;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

@SuppressWarnings({"unchecked", "rawtypes"})
@Converter
public class MongoDbBasicConverters {
    
    private static final transient Logger LOG = LoggerFactory.getLogger(MongoDbBasicConverters.class);

    // Jackson's ObjectMapper is thread-safe, so no need to create a pool nor synchronize access to it
    private static ObjectMapper objectMapper = null;
    
    // will attempt to load the Jackson ObjectMapper class using the class loader of MongoDbBasicConverters
    // in an OSGi environment, if Jackson is present it would have already been wired since Jackson is an optional dependency (Import-Package)
    static {
        try {
            Class<ObjectMapper> objectMapperC = (Class<ObjectMapper>) MongoDbBasicConverters.class.getClassLoader().loadClass("org.codehaus.jackson.map.ObjectMapper");
            MongoDbBasicConverters.objectMapper = objectMapperC.newInstance();
        } catch (Exception e) {
            // do nothing, Jackson not found
        }
    }

    @Converter
    public static DBObject fromMapToDBObject(Map<?, ?> map) {
        return new BasicDBObject(map);
    }
    
    @Converter
    public static Map<String, Object> fromBasicDBObjectToMap(BasicDBObject basicDbObject) {
        return (Map<String, Object>) basicDbObject;
    }
    
    @Converter
    public static DBObject fromStringToDBObject(String s) {
        DBObject answer = null;
        try {
            answer = (DBObject) JSON.parse(s);
        } catch (Exception e) {
            LOG.warn("String -> DBObject conversion selected, but the following exception occurred. Returning null.", e);
        }
        
        return answer;
    }
   
    @Converter
    public static DBObject fromAnyObjectToDBObject(Object value) {
        if (MongoDbBasicConverters.objectMapper == null) {
            LOG.warn("Conversion has fallen back to generic Object -> DBObject, but Jackson " +
            		"was not available on the classpath during initialization. Returning null.");
            return null;
        }
        
        BasicDBObject answer;
        try {
            Map m = MongoDbBasicConverters.objectMapper.convertValue(value, Map.class);
            answer = new BasicDBObject(m);
        } catch (Exception e) {
            LOG.warn("Conversion has fallen back to generic Object -> DBObject, but unable to convert type {}. Returning null.", 
                    value.getClass().getCanonicalName());
            return null;
        }
        return answer;
        
    }
    
}
