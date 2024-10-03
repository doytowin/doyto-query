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

package win.doyto.query.sql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.annotation.*;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.*;
import win.doyto.query.sql.field.FieldMapper;
import win.doyto.query.util.ColumnUtil;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static win.doyto.query.core.QuerySuffix.isValidValue;
import static win.doyto.query.sql.Constant.*;
import static win.doyto.query.util.CommonUtil.getField;
import static win.doyto.query.util.CommonUtil.readFieldGetter;

/**
 * BuildHelper
 *
 * @author f0rb on 2021-02-16
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BuildHelper {
    private static final Pattern PTN_SORT = Pattern.compile(",(asc|desc)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PTN_SHARP_EX = Pattern.compile("#\\{(\\w+)}");
    private static final Pattern PTN_DIGITS_END = Pattern.compile("\\d++$");

    static String resolveTableName(Class<?> entityClass) {
        String tableName;
        if (entityClass.isAnnotationPresent(CompositeView.class)) {
            CompositeView compositeViewAnno = entityClass.getAnnotation(CompositeView.class);
            tableName = resolveTableName(compositeViewAnno.value());
        } else if (entityClass.isAnnotationPresent(ComplexView.class)) {
            ComplexView complexView = entityClass.getAnnotation(ComplexView.class);
            tableName = resolveTableName(complexView.value());
        } else if (entityClass.isAnnotationPresent(View.class)) {
            View[] views = entityClass.getAnnotationsByType(View.class);
            tableName = resolveTableName(views);
        } else {
            tableName = defaultTableName(entityClass);
        }
        return tableName;
    }

    public static String resolveTableName(View... views) {
        return Arrays.stream(views)
                     .filter(view -> view.type() != ViewType.NESTED && !view.context())
                     .map(view -> {
                         String tableName = BuildHelper.defaultTableName(view.value());
                         String alias = view.alias();
                         return !alias.isEmpty() ? tableName + SPACE + alias : tableName;
                     }).collect(Collectors.joining(SEPARATOR));
    }

    static String defaultTableName(Class<?> entityClass) {
        if (entityClass.isAnnotationPresent(Entity.class)) {
            Entity entityAnno = entityClass.getAnnotation(Entity.class);
            return GlobalConfiguration.formatTable(entityAnno.name());
        }
        String entityName = entityClass.getSimpleName();
        entityName = StringUtils.removeEnd(entityName, "Entity");
        entityName = StringUtils.removeEnd(entityName, "View");
        return GlobalConfiguration.formatTable(entityName);
    }

    public static String resolveTableName(Class<?>[] value) {
        return Arrays.stream(value)
                     .map(BuildHelper::defaultTableName)
                     .collect(Collectors.joining(SEPARATOR));
    }

    static String buildStart(String[] columns, String table) {
        return SELECT + StringUtils.join(columns, SEPARATOR) + FROM + table + SPACE + TABLE_ALIAS;
    }

    public static String buildWhere(Query query, List<Object> argList) {
        return buildCondition(WHERE, query, argList);
    }

    public static String buildHaving(Having having, List<Object> argList) {
        Class<?> havingClass = having.getClass();
        Field[] fields = Arrays.stream(ColumnUtil.initFields(havingClass, FieldMapper::init))
                               .filter(f -> f.getDeclaringClass() == havingClass).toArray(Field[]::new);
        String clause = buildCondition(fields, having, argList, EMPTY, AND);
        return clause.isEmpty() ? clause : HAVING + clause;
    }

    public static String buildCondition(String prefix, Object query, List<Object> argList) {
        if (HAVING.equals(prefix)) {
            return buildHaving((Having) query, argList);
        }
        return buildCondition(prefix, query, argList, EMPTY);
    }

    public static String buildCondition(String prefix, Object query, List<Object> argList, String alias) {
        alias = StringUtils.isBlank(alias) ? EMPTY : alias + ".";
        Class<?> queryClass = query.getClass();
        if (Arrays.asList(queryClass.getInterfaces()).contains(Having.class)) {
            queryClass = queryClass.getSuperclass();
        }
        Field[] fields = ColumnUtil.initFields(queryClass, FieldMapper::init);
        String clause = buildCondition(fields, query, argList, alias, AND);
        return clause.isEmpty() ? clause : prefix + clause;
    }

    public static String buildCondition(Field[] fields, Object query, List<Object> argList, String alias, String connector) {
        StringJoiner conditionJoiner = new StringJoiner(connector);
        for (Field field : fields) {
            Object value = readFieldGetter(field, query);
            if (isValidValue(value, field)) {
                String condition = FieldMapper.execute(field, alias, argList, value);
                if (condition != null) {
                    conditionJoiner.add(condition);
                }
            }
        }
        return conditionJoiner.toString();
    }

    public static String buildOrderBy(DoytoQuery pageQuery) {
        return buildOrderBy(pageQuery, ORDER_BY);
    }

    static String buildOrderBy(DoytoQuery pageQuery, String orderBy) {
        if (pageQuery.getSort() == null) {
            return EMPTY;
        }
        return orderBy + PTN_SORT.matcher(pageQuery.getSort()).replaceAll(" $1").replace(";", SEPARATOR);
    }

    public static String buildLock(DoytoQuery pageQuery) {
        if (pageQuery.getLockMode() == LockMode.PESSIMISTIC_READ) {
            return GlobalConfiguration.dialect().forShare();
        } else if (pageQuery.getLockMode() == LockMode.PESSIMISTIC_WRITE) {
            return GlobalConfiguration.dialect().forUpdate();
        }
        return EMPTY;
    }

    public static String buildPaging(String sql, DoytoQuery pageQuery) {
        if (pageQuery.needPaging()) {
            int pageSize = pageQuery.getPageSize();
            int offset = GlobalConfiguration.calcOffset(pageQuery);
            sql = GlobalConfiguration.dialect().buildPageSql(sql, pageSize, offset);
        }
        return sql;
    }

    public static String buildPlaceHolders(int size) {
        return IntStream.range(0, size)
                        .mapToObj(i -> PLACE_HOLDER)
                        .collect(CommonUtil.CLT_COMMA_WITH_PAREN);
    }

    public static String replaceExpressionInString(String input, Object target, List<Object> args) {
        Matcher matcher = PTN_SHARP_EX.matcher(input);
        if (!matcher.find()) {
            return input;
        }

        StringBuilder sb = new StringBuilder();
        do {
            String fieldName = matcher.group(1);
            Object value = readFieldGetter(target, fieldName);

            QuerySuffix suffix = QuerySuffix.resolve(BuildHelper.resolveFieldName(fieldName));
            if (suffix == QuerySuffix.NONE) {
                matcher.appendReplacement(sb, "?");
                args.add(value);
            } else {
                Field field = getField(target, fieldName);
                FieldMapper.init(field);
                String ex = FieldMapper.execute(field, EMPTY, args, value);
                matcher.appendReplacement(sb, ex);
            }
        } while (matcher.find());
        return matcher.appendTail(sb).toString();
    }

    public static String resolveFieldName(String fieldName) {
        return PTN_DIGITS_END.matcher(fieldName).replaceFirst(EMPTY);
    }
}
