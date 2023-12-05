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

package win.doyto.query.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.annotation.*;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.AggregationPrefix;
import win.doyto.query.core.Dialect;
import win.doyto.query.core.Having;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * ColumnUtil
 *
 * @author f0rb on 2021-11-21
 */
@UtilityClass
public class ColumnUtil {

    private static final Pattern PTN_CAPITAL_CHAR = Pattern.compile("([A-Z])");
    private static final Map<Class<?>, Field[]> classFieldsMap = new ConcurrentHashMap<>();

    /**
     * For MongoDB implementation currently.
     */
    public static Field[] initFields(Class<?> clazz) {
        return initFields(clazz, null);
    }

    public static Field[] queryFields(Class<?> queryClass) {
        Field[] fields = classFieldsMap.get(queryClass);
        if (fields == null) {
            fields = filterFields(queryClass, ColumnUtil::shouldRetain).toArray(Field[]::new);
        }
        return fields;
    }

    public static Field[] initFields(Class<?> queryClass, Consumer<Field> fieldConsumer) {
        Field[] fields = classFieldsMap.get(queryClass);
        if (fields == null) {
            synchronized (classFieldsMap) {
                fields = classFieldsMap.get(queryClass);
                if (fields == null) {
                    fields = filterFields(queryClass, ColumnUtil::shouldRetain).toArray(Field[]::new);
                    if (fieldConsumer != null) {
                        Arrays.stream(fields).forEach(fieldConsumer);
                    }
                    classFieldsMap.put(queryClass, fields);
                }
            }
        }
        return fields;
    }

    public static Stream<Field> filterFields(Class<?> entityClass) {
        return filterFields(entityClass, ColumnUtil::filterForEntity);
    }

    public static Stream<Field> filterFields(Class<?> clazz, Predicate<Field> fieldFilter) {
        List<Class<?>> allClasses = ClassUtils.getAllSuperclasses(clazz);
        allClasses.remove(allClasses.size() - 1); // remove Object.class
        Collections.reverse(allClasses);
        allClasses.add(clazz); // add target class to the tail
        return allClasses.stream()
                         .flatMap(c -> Arrays.stream(c.getDeclaredFields()))
                         .filter(fieldFilter);
    }

    /**
     * Filter fields in an entity class
     *
     * @param entityClass the entityClass
     * @return unmodifiable fields without id and @Transient field
     */
    public static List<Field> getColumnFieldsFrom(Class<?> entityClass) {
        return filterFields(entityClass).toList();
    }

    public static String convertColumn(String columnName) {
        columnName = CommonUtil.camelize(columnName);
        return GlobalConfiguration.instance().isMapCamelCaseToUnderscore() ?
                camelCaseToUnderscore(columnName) : columnName;
    }

    public static String convertTableName(String domain) {
        return camelCaseToUnderscore(CommonUtil.camelize(domain));
    }

    private static String camelCaseToUnderscore(String camel) {
        return PTN_CAPITAL_CHAR.matcher(camel).replaceAll("_$1").toLowerCase();
    }

    public static String resolveColumn(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null && !column.name().isEmpty()) {
            return column.name();
        }
        return resolveColumn(field.getName());
    }

    public static String resolveColumn(String fieldName) {
        AggregationPrefix aggregationPrefix = AggregationPrefix.resolveField(fieldName);
        String columnName = aggregationPrefix.resolveColumnName(fieldName);
        columnName = convertColumn(columnName);
        columnName = GlobalConfiguration.dialect().wrapLabel(columnName);
        if (aggregationPrefix != AggregationPrefix.NONE) {
            columnName = aggregationPrefix.getName() + "(" + columnName + ")";
        }
        return columnName;
    }

    public static String selectAs(Field field) {
        String columnName = resolveColumn(field);
        Dialect dialect = GlobalConfiguration.dialect();
        String fieldName = dialect.wrapLabel(field.getName());
        return columnName.equalsIgnoreCase(fieldName) ? columnName : columnName + " AS " + fieldName;
    }

    public static String[] resolveSelectColumns(Class<?> entityClass) {
        return resolveSelectColumnStream(entityClass).toArray(String[]::new);
    }

    public static Stream<String> resolveSelectColumnStream(Class<?> entityClass) {
        return FieldUtils.getAllFieldsList(entityClass).stream()
                         .filter(ColumnUtil::shouldRetain)
                         .map(ColumnUtil::selectAs);
    }

    public static boolean filterForEntity(Field field) {
        return shouldRetain(field)
                && !field.isAnnotationPresent(GeneratedValue.class) // ignore id
                && !field.isAnnotationPresent(DomainPath.class)     // ignore subdomains
                ;
    }

    public static boolean filterForJoinEntity(Field field) {
        return shouldRetain(field)
                && !field.isAnnotationPresent(DomainPath.class)    // ignore join field
                ;
    }

    public static boolean shouldRetain(Field field) {
        return !field.getName().startsWith("$")                  // $jacocoData
                && !Modifier.isStatic(field.getModifiers())      // static field
                // Transient field, won't be used in where condition
                && !field.isAnnotationPresent(Transient.class)
                && !field.isAnnotationPresent(Join.class)
                // Having field, will be used in having condition only
                && !Having.class.isAssignableFrom(field.getType())
                ;
    }

    public static boolean isSingleColumn(String... columns) {
        return columns.length == 1 && !columns[0].contains(",");
    }

    public static String[] resolveIdColumn(Class<?> entityClass) {
        return Arrays.stream(FieldUtils.getFieldsWithAnnotation(entityClass, Id.class))
                     .map(ColumnUtil::resolveColumn).toArray(String[]::new);
    }
}
