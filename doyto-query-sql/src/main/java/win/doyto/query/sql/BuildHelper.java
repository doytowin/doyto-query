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

package win.doyto.query.sql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.annotation.CompositeView;
import win.doyto.query.annotation.Entity;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.LockMode;
import win.doyto.query.entity.Persistable;
import win.doyto.query.sql.field.FieldMapper;
import win.doyto.query.util.ColumnUtil;
import win.doyto.query.util.CommonUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static win.doyto.query.core.QuerySuffix.isValidValue;
import static win.doyto.query.sql.Constant.*;
import static win.doyto.query.util.CommonUtil.readFieldGetter;

/**
 * BuildHelper
 *
 * @author f0rb on 2021-02-16
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BuildHelper {
    private static final Pattern PTN_SORT = Pattern.compile(",(asc|desc)", Pattern.CASE_INSENSITIVE);

    static String resolveTableName(Class<?> entityClass) {
        String tableName;
        if (entityClass.isAnnotationPresent(Entity.class)) {
            Entity entityAnno = entityClass.getAnnotation(Entity.class);
            tableName = GlobalConfiguration.formatTable(entityAnno.name());
        } else if (entityClass.isAnnotationPresent(CompositeView.class)) {
            CompositeView compositeViewAnno = entityClass.getAnnotation(CompositeView.class);
            tableName = resolveTableName(compositeViewAnno.value());
        } else {
            tableName = defaultTableName(entityClass);
        }
        return tableName;
    }

    static String defaultTableName(Class<?> entityClass) {
        String entityName = entityClass.getSimpleName();
        entityName = StringUtils.removeEnd(entityName, "Entity");
        entityName = StringUtils.removeEnd(entityName, "View");
        return GlobalConfiguration.formatTable(entityName);
    }

    public static String resolveTableName(Class<? extends Persistable<? extends Serializable>>[] value) {
        return Arrays.stream(value)
                     .map(BuildHelper::resolveTableName)
                     .collect(Collectors.joining(SEPARATOR));
    }

    static String buildStart(String[] columns, String table) {
        return SELECT + StringUtils.join(columns, SEPARATOR) + FROM + table + SPACE + TABLE_ALIAS;
    }

    public static String buildWhere(DoytoQuery query, List<Object> argList) {
        return buildCondition(WHERE, query, argList);
    }

    public static String buildCondition(String prefix, Object query, List<Object> argList) {
        return buildCondition(prefix, query, argList, EMPTY);
    }

    public static String buildCondition(String prefix, Object query, List<Object> argList, String alias) {
        alias = StringUtils.isBlank(alias) ? EMPTY : alias + ".";
        Field[] fields = ColumnUtil.initFields(query.getClass(), FieldMapper::init);
        String clause = buildCondition(fields, query, argList, alias, AND);
        return clause.isEmpty() ? clause : prefix + clause;
    }

    public static String buildCondition(Field[] fields, Object query, List<Object> argList, String alias, String connector) {
        StringJoiner whereJoiner = new StringJoiner(connector);
        for (Field field : fields) {
            Object value = readFieldGetter(field, query);
            if (isValidValue(value, field)) {
                String and = FieldMapper.execute(field, alias, argList, value);
                if (and != null) {
                    whereJoiner.add(and);
                }
            }
        }
        return whereJoiner.toString();
    }

    public static String buildOrderBy(DoytoQuery pageQuery) {
        return buildOrderBy(pageQuery, " ORDER BY ");
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

}
