/*
 * Copyright © 2019-2023 Forb Yuan
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

package win.doyto.query.jdbc.rowmapper;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.util.ColumnUtil;
import win.doyto.query.util.CommonUtil;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * BeanPropertyRowMapper
 *
 * @author f0rb on 2021-10-26
 */
@Slf4j
public class BeanPropertyRowMapper<E> implements RowMapper<E> {
    private final Class<E> mappedClass;
    private final Map<String, PropertyDescriptor> fieldMap = new LinkedHashMap<>();

    @SneakyThrows
    public BeanPropertyRowMapper(Class<E> mappedClass) {
        this.mappedClass = mappedClass;
        BeanInfo beanInfo = Introspector.getBeanInfo(mappedClass);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        Arrays.stream(propertyDescriptors)
              .filter(pd -> pd.getWriteMethod() != null
                      && ColumnUtil.filterForJoinEntity(FieldUtils.getField(mappedClass, pd.getName(), true))
              )
              .forEach(pd -> this.fieldMap.put(pd.getName(), pd));
    }

    @Override
    @SneakyThrows
    public E map(ResultSet rs, int rn) throws SQLException {
        E entity = mappedClass.getDeclaredConstructor().newInstance();

        ResultSetMetaData rsmd = rs.getMetaData();
        for (int i = 0; i++ < rsmd.getColumnCount();) {
            String columnName = rsmd.getColumnName(i);
            String fieldName = CommonUtil.toCamelCase(columnName.toLowerCase());
            PropertyDescriptor pd = fieldMap.get(fieldName);
            if (pd == null) {
                log.warn("Column [{}:{}] not found in {}.", columnName, fieldName, fieldMap.keySet());
                continue;
            }
            try {
                Object value = getColumnValue(rs, pd);
                pd.getWriteMethod().invoke(entity, value);
            } catch (IllegalArgumentException | NoSuchElementException e) {
                log.error("Fail to get value for [{}]: {}", pd.getName(), e.getMessage());
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("Fail to invoke write method: {}-{}", pd.getWriteMethod().getName(), e.getMessage());
            }
        }
        return entity;
    }

    protected Object getColumnValue(ResultSet rs, PropertyDescriptor pd) throws SQLException {
        String columnLabel = pd.getName();
        try {
            if (rs.getObject(columnLabel) == null) return null;
        } catch (SQLException e) {
            throw new NoSuchElementException(e);
        }
        Class<?> propertyType = pd.getPropertyType();
        if (propertyType.isEnum()) {
            String value = rs.getString(columnLabel);
            if (NumberUtils.isCreatable(value)) {
                Integer ordinal = NumberUtils.createInteger(value);
                return propertyType.getEnumConstants()[ordinal];
            }
            return stringToEnum(propertyType, value);
        }
        if (propertyType == LocalDateTime.class) {
            Timestamp timestamp = rs.getTimestamp(columnLabel);
            return timestamp != null ? timestamp.toLocalDateTime() : null;
        }
        return rs.getObject(columnLabel, propertyType);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> T stringToEnum(Class<?> propertyType, String value) {
        return Enum.valueOf((Class<T>) propertyType, value);
    }
}
