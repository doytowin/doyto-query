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

package win.doyto.query.sql;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.annotation.Aggregation;
import win.doyto.query.annotation.Joins;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.util.ColumnUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.persistence.Table;

import static win.doyto.query.sql.Constant.*;
import static win.doyto.query.util.CommonUtil.*;

/**
 * EntityMetadata
 *
 * @author f0rb on 2021-12-28
 */
@Getter
@Setter
@SuppressWarnings("java:S1874")
public class EntityMetadata {
    private static final Map<Class<?>, EntityMetadata> holder = new HashMap<>();
    private static final Pattern PTN_PLACE_HOLDER = Pattern.compile("#\\{(\\w+)}");

    private Class<?> entityClass;
    private String columnsForSelect;
    private String tableName;
    private String joinSql = "";
    private String groupByColumns = "";
    private String groupBySql = "";

    public EntityMetadata(Class<?> entityClass) {
        this.entityClass = entityClass;
        this.tableName = entityClass.getAnnotation(Table.class).name();

        this.columnsForSelect = buildSelectColumns(entityClass);
        buildJoinSql(entityClass);
        buildGroupBySql(entityClass);
    }

    static EntityMetadata build(Class<?> entityClass) {
        return holder.computeIfAbsent(entityClass, EntityMetadata::new);
    }

    private String buildSelectColumns(Class<?> entityClass) {
        return ColumnUtil.filterFields(entityClass)
                         .map(ColumnUtil::selectAs)
                         .collect(Collectors.joining(SEPARATOR));
    }

    private void buildJoinSql(Class<?> entityClass) {
        if (entityClass.isAnnotationPresent(Joins.class)) {
            String[] joins = this.entityClass.getAnnotation(Joins.class).value();
            joinSql = String.join(SPACE, joins);
        }
    }

    private void buildGroupBySql(Class<?> entityClass) {
        if (entityClass.isAnnotationPresent(Aggregation.class)) {
            Aggregation aggregation = entityClass.getAnnotation(Aggregation.class);
            groupByColumns = StringUtils.join(aggregation.groupBy(), SEPARATOR);
            groupBySql = " GROUP BY " + groupByColumns;
            if (!aggregation.having().isEmpty()) {
                groupBySql += " HAVING " + aggregation.having();
            }
        }
    }

    public String resolveJoinSql(DoytoQuery query, List<Object> argList) {
        return resolveJoin(query, argList, this.joinSql);
    }

    private static String resolveJoin(Object query, List<Object> argList, String join) {
        if (join.isEmpty()) {
            return "";
        }
        Matcher matcher = PTN_PLACE_HOLDER.matcher(join);

        StringBuffer sb = new StringBuffer(SPACE);
        while (matcher.find()) {
            String fieldName = matcher.group(1);
            Field field = getField(query, fieldName);
            Object value = readField(field, query);
            argList.add(value);
            writeField(field, query, null);
            matcher.appendReplacement(sb, PLACE_HOLDER);
        }

        return matcher.appendTail(sb).toString();
    }
}
