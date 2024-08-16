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

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.annotation.*;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.Having;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
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
        return classFieldsMap.computeIfAbsent(queryClass, ColumnUtil::filterFields);
    }

    public static Field[] initFields(Class<?> queryClass, Consumer<Field> fieldConsumer) {
        Field[] fields = classFieldsMap.get(queryClass);
        if (fields == null) {
            synchronized (classFieldsMap) {
                fields = classFieldsMap.get(queryClass);
                if (fields == null) {
                    fields = filterFields(queryClass);
                    if (fieldConsumer != null) {
                        Arrays.stream(fields).forEach(fieldConsumer);
                    }
                    classFieldsMap.put(queryClass, fields);
                }
            }
        }
        return fields;
    }

    private static Field[] filterFields(Class<?> queryClass) {
        return filterFields(queryClass, ColumnUtil::shouldRetain).toArray(Field[]::new);
    }

    public static Stream<Field> filterFields(Class<?> clazz, Predicate<Field> fieldFilter) {
        return withSuperclasses(clazz)
                .stream()
                .flatMap(c -> Arrays.stream(c.getDeclaredFields()))
                .filter(fieldFilter);
    }

    public static List<Class<?>> withSuperclasses(Class<?> clazz) {
        LinkedList<Class<?>> classes = new LinkedList<>();
        Class<?> superclass = clazz;
        do {
            classes.addFirst(superclass);
            superclass = superclass.getSuperclass();
        } while (superclass != Object.class);
        return classes;
    }

    /**
     * Filter fields in an entity class
     *
     * @param entityClass the entityClass
     * @return unmodifiable fields without id and @Transient field
     */
    public static List<Field> getColumnFieldsFrom(Class<?> entityClass) {
        return filterFields(entityClass, ColumnUtil::filterForEntity).toList();
    }

    public static String convertColumn(String columnName) {
        columnName = StringUtils.uncapitalize(columnName);
        return GlobalConfiguration.instance().isMapCamelCaseToUnderscore() ?
                camelCaseToUnderscore(columnName) :
                CommonUtil.toCamelCase(columnName);
    }

    public static String convertTableName(String domain) {
        return camelCaseToUnderscore(StringUtils.uncapitalize(domain));
    }

    public static String camelCaseToUnderscore(String camel) {
        return PTN_CAPITAL_CHAR.matcher(camel).replaceAll("_$1").toLowerCase();
    }

    public static String resolveColumn(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null && !column.name().isEmpty()) {
            return column.name();
        }
        String columnName = ColumnUtil.convertColumn(field.getName());
        return GlobalConfiguration.dialect().wrapLabel(columnName);
    }

    public static boolean filterForEntity(Field field) {
        return shouldRetain(field)
                && !field.isAnnotationPresent(GeneratedValue.class) // ignore id
                && !field.isAnnotationPresent(DomainPath.class)     // ignore subdomains
                ;
    }

    public static boolean filterForView(Field field) {
        return !field.isAnnotationPresent(DomainPath.class)      // ignore subdomains
                && shouldRetain(field);
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

    public static List<Field> resolveDomainPathFields(Class<?> entityClass) {
        return FieldUtils.getAllFieldsList(entityClass).stream()
                         .filter(joinField -> joinField.isAnnotationPresent(DomainPath.class)).toList();
    }

    public static boolean isSingleColumn(String... columns) {
        return columns.length == 1 && !columns[0].contains(",");
    }

    public static String[] resolveIdColumn(Class<?> entityClass) {
        return Arrays.stream(FieldUtils.getFieldsWithAnnotation(entityClass, Id.class))
                     .map(ColumnUtil::resolveColumn).toArray(String[]::new);
    }
}
