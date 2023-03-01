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
import win.doyto.query.annotation.CompositeView;
import win.doyto.query.annotation.ForeignKey;
import win.doyto.query.annotation.GroupBy;
import win.doyto.query.annotation.NestedView;
import win.doyto.query.util.ColumnUtil;

import java.util.*;
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
    private final String joinConditions;
    @Getter
    private final String groupByColumns;
    @Getter
    private final String groupBySql;
    @Getter
    private EntityMetadata nested;

    public EntityMetadata(Class<?> entityClass) {
        if (entityClass.isAnnotationPresent(NestedView.class)) {
            NestedView anno = entityClass.getAnnotation(NestedView.class);
            Class<?> clazz = anno.value();
            this.nested = EntityMetadata.build(clazz);
            //tableName = OP + SELECT +
            //        entityMetadata.getColumnsForSelect() +
            //        FROM + entityMetadata.getTableName() +
            //        entityMetadata.getJoinConditions() +
            //        CP + AS +
            this.tableName = BuildHelper.defaultTableName(clazz);
        } else {
            this.tableName = BuildHelper.resolveTableName(entityClass);
        }
        this.joinConditions = resolveJoinConditions(entityClass);
        this.columnsForSelect = buildSelectColumns(entityClass);
        this.groupByColumns = resolveGroupByColumns(entityClass);
        this.groupBySql = buildGroupBySql(groupByColumns);
    }

    public static List<String> resolveEntityRelations(Class<?>[] viewClasses, Set<Object> parentColumns) {
        List<String> relations = new ArrayList<>();
        for (int i = 0, len = viewClasses.length; i < len; i++) {
            List<Class<?>> otherClassList = new ArrayList<>(Arrays.asList(viewClasses));
            otherClassList.remove(i);
            Arrays.stream(ColumnUtil.initFields(viewClasses[i]))
                  .filter(field -> field.isAnnotationPresent(ForeignKey.class))
                  .forEach(field -> {
                      ForeignKey fkAnno = field.getAnnotation(ForeignKey.class);
                      if (parentColumns.contains(fkAnno.field()) || otherClassList.contains(fkAnno.entity())) {
                          String c1 = ColumnUtil.convertColumn(field.getName());
                          String c2 = ColumnUtil.convertColumn(fkAnno.field());
                          relations.add(c1 + EQUAL + c2);
                      }
                  });
        }
        return relations;
    }

    static String resolveJoinConditions(Class<?> viewClass) {
        List<String> conditions = new ArrayList<>();
        CompositeView compositeViewAnno = viewClass.getAnnotation(CompositeView.class);
        if (compositeViewAnno != null && compositeViewAnno.value().length > 0) {
            conditions.addAll(resolveEntityRelations(compositeViewAnno.value(), new HashSet<>()));
        }
        return conditions.isEmpty() ? EMPTY : WHERE + String.join(AND, conditions);
    }

    static EntityMetadata build(Class<?> entityClass) {
        return holder.computeIfAbsent(entityClass, EntityMetadata::new);
    }

    static String buildSelectColumns(Class<?> entityClass) {
        return ColumnUtil.filterFields(entityClass)
                         .map(ColumnUtil::selectAs)
                         .collect(Collectors.joining(SEPARATOR));
    }

    static String resolveGroupByColumns(Class<?> entityClass) {
        return ColumnUtil.filterFields(entityClass, field -> field.isAnnotationPresent(GroupBy.class))
                         .map(field -> ColumnUtil.convertColumn(field.getName()))
                         .collect(Collectors.joining(SEPARATOR));
    }

    static String buildGroupBySql(String groupByColumns) {
        return !groupByColumns.isEmpty() ? " GROUP BY " + groupByColumns : "";
    }

}
