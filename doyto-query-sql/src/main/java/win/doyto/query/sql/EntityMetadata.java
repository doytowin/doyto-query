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

import lombok.Getter;
import win.doyto.query.annotation.ForeignKey;
import win.doyto.query.annotation.GroupBy;
import win.doyto.query.annotation.View;
import win.doyto.query.util.ColumnUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static win.doyto.query.sql.Constant.*;

/**
 * EntityMetadata
 *
 * @author f0rb on 2021-12-28
 */
@SuppressWarnings("java:S1874")
public class EntityMetadata {
    private static final Map<Class<?>, EntityMetadata> holder = new ConcurrentHashMap<>();

    @Getter
    private final String columnsForSelect;
    @Getter
    private final String tableName;
    @Getter
    private final String relations;
    @Getter
    private String groupByColumns = "";
    @Getter
    private final String groupBySql;

    public EntityMetadata(Class<?> entityClass) {
        this.tableName = BuildHelper.resolveTableName(entityClass);
        this.relations = resolveEntityRelations(entityClass);

        this.columnsForSelect = buildSelectColumns(entityClass);
        this.groupBySql = buildGroupBySql(entityClass);
    }

    private String resolveEntityRelations(Class<?> viewClass) {
        List<String> conditions = new ArrayList<>();
        View viewAnno = viewClass.getAnnotation(View.class);
        if (viewAnno != null && viewAnno.value().length > 0) {
            Class<?>[] viewClasses = viewAnno.value();
            for (int i = 1; i < viewClasses.length; i++) {
                for (Field field : ColumnUtil.initFields(viewClasses[i])) {
                    if (field.isAnnotationPresent(ForeignKey.class)) {
                        ForeignKey fkAnno = field.getAnnotation(ForeignKey.class);
                        conditions.add(fkAnno.field() + " = " + field.getName());
                    }
                }
            }
        }
        return conditions.isEmpty() ? EMPTY : String.join(AND, conditions) + AND;
    }

    static EntityMetadata build(Class<?> entityClass) {
        return holder.computeIfAbsent(entityClass, EntityMetadata::new);
    }

    private String buildSelectColumns(Class<?> entityClass) {
        return ColumnUtil.filterFields(entityClass)
                         .map(ColumnUtil::selectAs)
                         .collect(Collectors.joining(SEPARATOR));
    }

    private String buildGroupBySql(Class<?> entityClass) {
        String groupBy = "";

        groupByColumns = ColumnUtil.filterFields(entityClass, field -> field.isAnnotationPresent(GroupBy.class))
                                   .map(field -> ColumnUtil.convertColumn(field.getName()))
                                   .collect(Collectors.joining(SEPARATOR));
        if (!groupByColumns.isEmpty()) {
            groupBy = " GROUP BY " + this.groupByColumns;
        }
        return groupBy;
    }

}
