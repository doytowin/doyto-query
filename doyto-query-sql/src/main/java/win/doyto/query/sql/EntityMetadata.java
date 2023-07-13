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
import win.doyto.query.annotation.*;
import win.doyto.query.util.ColumnUtil;

import javax.persistence.Column;
import java.util.ArrayList;
import java.util.Arrays;
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
@Getter
public class EntityMetadata {
    private static final Map<Class<?>, EntityMetadata> holder = new ConcurrentHashMap<>();

    private final String columnsForSelect;
    private final String tableName;
    private final String joinConditions;
    private final String groupByColumns;
    private final String groupBySql;
    private EntityMetadata nested;

    public EntityMetadata(Class<?> entityClass) {
        if (entityClass.isAnnotationPresent(NestedView.class)) {
            NestedView anno = entityClass.getAnnotation(NestedView.class);
            Class<?> clazz = anno.value();
            // We don't need to cache the nested EntityMetadata,
            // since the host EntityMetadata is already cached.
            this.nested = new EntityMetadata(clazz);
            this.tableName = BuildHelper.defaultTableName(clazz);
        } else {
            this.tableName = BuildHelper.resolveTableName(entityClass);
        }
        this.joinConditions = resolveJoinConditions(entityClass);
        this.columnsForSelect = buildSelectColumns(entityClass);
        this.groupByColumns = resolveGroupByColumns(entityClass);
        this.groupBySql = buildGroupBySql(groupByColumns);
    }

    public static List<String> resolveEntityRelations(Class<?>[] viewClasses) {
        List<ViewIndex> viewIndices = Arrays.stream(viewClasses).map(ViewIndex::new).collect(Collectors.toList());
        return resolveEntityRelations(viewIndices);
    }

    public static List<String> resolveEntityRelations(View[] views) {
        List<ViewIndex> viewIndices = Arrays.stream(views).map(ViewIndex::new).collect(Collectors.toList());
        return resolveEntityRelations(viewIndices);
    }

    private static List<String> resolveEntityRelations(List<ViewIndex> viewIndices) {
        List<String> relations = new ArrayList<>();
        viewIndices.forEach(currentViewIndex -> {
            currentViewIndex.voteDown();
            // iterate the fields of current table to compare with the rest tables
            // to build connection conditions
            Arrays.stream(ColumnUtil.initFields(currentViewIndex.getEntity()))
                  .filter(field -> field.isAnnotationPresent(ForeignKey.class))
                  .forEach(field -> {
                      ForeignKey fkAnno = field.getAnnotation(ForeignKey.class);
                      ViewIndex viewIndex = ViewIndex.searchEntity(viewIndices, fkAnno.entity());
                      if (viewIndex != null) {
                          String c1 = ColumnUtil.convertColumn(field.getName());
                          String c2 = ColumnUtil.convertColumn(fkAnno.field());
                          String alias1 = currentViewIndex.getAlias();
                          String alias2 = viewIndex.getAlias();
                          relations.add(alias1 + c1 + EQUAL + alias2 + c2);
                      }
                  });
            currentViewIndex.voteUp();
        });
        return relations;
    }

    static String resolveJoinConditions(Class<?> viewClass) {
        List<String> conditions = new ArrayList<>();
        if (viewClass.isAnnotationPresent(CompositeView.class)) {
            CompositeView viewAnno = viewClass.getAnnotation(CompositeView.class);
            assert viewAnno.value().length > 0;
            conditions.addAll(resolveEntityRelations(viewAnno.value()));
        } else if (viewClass.isAnnotationPresent(ComplexView.class)) {
            ComplexView viewAnno = viewClass.getAnnotation(ComplexView.class);
            assert viewAnno.value().length > 0;
            conditions.addAll(resolveEntityRelations(viewAnno.value()));
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
                         .map(field -> {
                             if (field.isAnnotationPresent(Column.class)) {
                                 return field.getAnnotation(Column.class).name();
                             } else {
                                 return ColumnUtil.convertColumn(field.getName());
                             }
                         })
                         .collect(Collectors.joining(SEPARATOR));
    }

    static String buildGroupBySql(String groupByColumns) {
        return !groupByColumns.isEmpty() ? GROUP_BY + groupByColumns : "";
    }

}
