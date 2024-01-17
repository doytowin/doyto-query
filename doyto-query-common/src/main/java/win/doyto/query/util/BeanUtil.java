/*
 * Copyright © 2019-2024 Forb Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.doyto.query.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import win.doyto.query.geo.GeoShape;
import win.doyto.query.geo.GeoShapeDeserializer;
import win.doyto.query.geo.Point;
import win.doyto.query.geo.PointDeserializer;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * BeanUtil
 *
 * @author f0rb
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanUtil {

    private static final ObjectMapper objectMapper;
    private static final ObjectMapper objectMapper2;

    static {
        objectMapper = configObjectMapper(new ObjectMapper());
        objectMapper2 = objectMapper
                .copy()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static ObjectMapper configObjectMapper(ObjectMapper objectMapper) {
        objectMapper.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
                    .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
                    .enable(JsonParser.Feature.IGNORE_UNDEFINED)
                    .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .registerModule(buildGeoModule())
                    .registerModule(new JavaTimeModule());
        SimpleModule bsonModule = buildBsonModule();
        if (bsonModule != null) {
            objectMapper.registerModule(bsonModule);
        }
        return objectMapper;
    }

    private static SimpleModule buildGeoModule() {
        SimpleModule geoModule = new SimpleModule("Geo", new Version(1, 0, 0, "", "win.doyto", "doyto-query-geo"));
        geoModule.addDeserializer(Point.class, new PointDeserializer());
        geoModule.addDeserializer(GeoShape.class, new GeoShapeDeserializer());
        return geoModule;
    }

    @SuppressWarnings("unchecked")
    private static <T> SimpleModule buildBsonModule() {
        try {
            Class<T> bson = (Class<T>) ClassUtils.getClass("org.bson.conversions.Bson");
            Class<JsonDeserializer<T>> bsonDeserializer = (Class<JsonDeserializer<T>>) ClassUtils.getClass("win.doyto.query.mongodb.entity.BsonDeserializer");
            SimpleModule mod = new SimpleModule(bson.getName(), new Version(1, 0, 0, "", "win.doyto", "doyto-query-code"));
            mod.addDeserializer(bson, ConstructorUtils.invokeConstructor(bsonDeserializer));
            return mod;
        } catch (Exception e) {// ignore
            return null;
        }
    }

    public static Type[] getActualTypeArguments(Class<?> clazz) {
        Type genericSuperclass = clazz;
        do {
            if (genericSuperclass == null) return new Type[0];
            genericSuperclass = ((Class<?>) genericSuperclass).getGenericSuperclass();
        } while (!(genericSuperclass instanceof ParameterizedType));
        return ((ParameterizedType) genericSuperclass).getActualTypeArguments();
    }

    public static <T> T loadJsonData(String path, TypeReference<T> typeReference) throws IOException {
        return loadJsonData(typeReference.getClass().getResourceAsStream(path), typeReference);
    }

    public static <T> T loadJsonData(InputStream resourceAsStream, TypeReference<T> typeReference) throws IOException {
        return objectMapper.readValue(resourceAsStream, typeReference);
    }

    @SneakyThrows
    public static String stringify(Object target) {
        return objectMapper2.writeValueAsString(target);
    }

    @SneakyThrows
    public static <T> T parse(String json, TypeReference<T> typeReference) {
        return objectMapper.readValue(json, typeReference);
    }

    @SneakyThrows
    public static <T> T parse(String json, Class<T> clazz) {
        return objectMapper.readValue(json, clazz);
    }

    @SneakyThrows
    public static <T> T convertTo(Object source, TypeReference<T> typeReference) {
        return objectMapper.readValue(objectMapper.writeValueAsBytes(source), typeReference);
    }

    @SneakyThrows
    public static <T> T convertTo(Object source, Class<T> targetType) {
        return objectMapper.readValue(objectMapper.writeValueAsBytes(source), targetType);
    }

    @SneakyThrows
    public static <T> T convertToIgnoreNull(Object source, TypeReference<T> typeReference) {
        return objectMapper2.readValue(objectMapper2.writeValueAsBytes(source), typeReference);
    }

    @SneakyThrows
    public static <T> T convertToIgnoreNull(Object source, Class<T> targetType) {
        return objectMapper2.readValue(objectMapper2.writeValueAsBytes(source), targetType);
    }

    @SneakyThrows
    public static <T> T copyTo(Object from, T to) {
        return objectMapper.updateValue(to, from);
    }

    @SneakyThrows
    public static <T> T copyNonNull(Object from, T to) {
        return objectMapper2.updateValue(to, from);
    }

    public static <I> Class<I> getIdClass(Class<?> entityClass) {
        return getIdClass(entityClass, "id");
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <I> Class<I> getIdClass(Class<?> entityClass, String idName) {
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(idName, entityClass);
        return (Class<I>) propertyDescriptor.getPropertyType();
    }

}
