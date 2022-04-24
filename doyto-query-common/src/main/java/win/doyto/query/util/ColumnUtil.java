/*
 * Copyright © 2019-2022 Forb Yuan
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
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.Dialect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * ColumnUtil
 *
 * @author f0rb on 2021-11-21
 */
@UtilityClass
public class ColumnUtil {

    private static final Pattern PTN_CAPITAL_CHAR = Pattern.compile("([A-Z])");
    private static final Map<Class<?>, Field[]> classFieldsMap = new ConcurrentHashMap<>();

    public static Field[] initFields(Class<?> queryClass) {
        return initFields(queryClass, field -> {});
    }

    public static Field[] initFields(Class<?> queryClass, Consumer<Field> fieldConsumer) {
        classFieldsMap.computeIfAbsent(queryClass, c -> {
            Field[] fields = filterFields(c).toArray(Field[]::new);
            Arrays.stream(fields).forEach(fieldConsumer);
            return fields;
        });
        return classFieldsMap.get(queryClass);
    }

    public static Stream<Field> filterFields(Class<?> clazz) {
        List<Class<?>> allClasses = ClassUtils.getAllSuperclasses(clazz);
        allClasses.remove(allClasses.size() - 1);
        Collections.reverse(allClasses);
        allClasses.add(clazz);
        return allClasses.stream()
                         .flatMap(c -> Arrays.stream(c.getDeclaredFields()))
                         .filter(ColumnUtil::filterForEntity);
    }

    /**
     * Filter fields in an entity class
     *
     * @param entityClass the entityClass
     * @return unmodifiable fields without id and @Transient field
     */
    public static List<Field> getColumnFieldsFrom(Class<?> entityClass) {
        List<Field> fields = filterFields(entityClass).collect(Collectors.toList());
        return Collections.unmodifiableList(fields);
    }

    public static String convertColumn(String columnName) {
        return GlobalConfiguration.instance().isMapCamelCaseToUnderscore() ?
                camelCaseToUnderscore(columnName) : columnName;
    }

    private static String camelCaseToUnderscore(String camel) {
        return PTN_CAPITAL_CHAR.matcher(camel).replaceAll("_$1").toLowerCase();
    }

    public static String resolveColumn(Field field) {
        Column column = field.getAnnotation(Column.class);
        String columnName = column != null && !column.name().isEmpty() ? column.name() : convertColumn(field.getName());
        return GlobalConfiguration.dialect().wrapLabel(columnName);
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
        return shouldRetain(field) &&
                !field.isAnnotationPresent(GeneratedValue.class) // ignore id
                ;
    }

    public static boolean shouldRetain(Field field) {
        return !field.getName().startsWith("$")                  // $jacocoData
                && !Modifier.isStatic(field.getModifiers())      // static field
                // Transient field, won't be used in where condition
                && !field.isAnnotationPresent(Transient.class)
                ;
    }

    public static boolean isSingleColumn(String... columns) {
        return columns.length == 1 && !columns[0].contains(",");
    }

    public static String resolveIdColumn(Class<?> entityClass) {
        return resolveColumn(FieldUtils.getFieldsWithAnnotation(entityClass, Id.class)[0]);
    }
}
