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
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.util.ColumnUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import javax.persistence.Entity;

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
        Entity entity = entityClass.getAnnotation(Entity.class);
        if (entity != null) {
            tableName = GlobalConfiguration.formatTable(entity.name());
        } else {
            String entityName = entityClass.getSimpleName();
            entityName = StringUtils.removeEnd(entityName, "Entity");
            entityName = StringUtils.removeEnd(entityName, "View");
            tableName = GlobalConfiguration.formatTable(entityName);
        }
        return tableName;
    }

    static String buildStart(String[] columns, String table) {
        return Constant.SELECT + StringUtils.join(columns, SEPARATOR) + FROM + table + SPACE + TABLE_ALIAS;
    }

    public static String buildWhere(DoytoQuery query, List<Object> argList) {
        return buildCondition(WHERE, query, argList);
    }

    public static String buildCondition(String prefix, Object query, List<Object> argList) {
        return buildCondition(query, argList, prefix, EMPTY);
    }

    public static String buildCondition(Object query, List<Object> argList, String prefix, String alias) {
        alias = StringUtils.isBlank(alias) ? EMPTY : alias + ".";
        Field[] fields = ColumnUtil.initFields(query.getClass(), FieldProcessor::init);
        StringJoiner whereJoiner = new StringJoiner(AND);
        for (Field field : fields) {
            Object value = readFieldGetter(field, query);
            if (isValidValue(value, field)) {
                String and = FieldProcessor.execute(field, argList, value);
                if (and != null) {
                    whereJoiner.add(alias + and);
                }
            }
        }
        if (whereJoiner.length() == 0) {
            return EMPTY;
        }
        return prefix + whereJoiner;
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

    public static String buildPaging(String sql, DoytoQuery pageQuery) {
        if (pageQuery.needPaging()) {
            int pageSize = pageQuery.getPageSize();
            int offset = GlobalConfiguration.calcOffset(pageQuery);
            sql = GlobalConfiguration.dialect().buildPageSql(sql, pageSize, offset);
        }
        return sql;
    }

}
