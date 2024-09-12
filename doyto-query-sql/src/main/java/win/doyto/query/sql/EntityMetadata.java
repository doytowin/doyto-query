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

import lombok.Getter;
import win.doyto.query.annotation.*;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.AggregationPrefix;
import win.doyto.query.core.Dialect;
import win.doyto.query.util.ColumnUtil;

import java.lang.reflect.Field;
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

    private final Class<?> viewClass;
    private final String columnsForSelect;
    private final String tableName;
    private final String joinConditions;
    private final String groupByColumns;
    private final String groupBySql;
    private final List<View> withViews;
    private final List<Field> domainPathFields;
    private EntityMetadata nested;

    public EntityMetadata(Class<?> viewClass) {
        this.viewClass = viewClass;
        if (viewClass.isAnnotationPresent(NestedView.class)) {
            NestedView anno = viewClass.getAnnotation(NestedView.class);
            Class<?> clazz = anno.value();
            // We don't need to cache the nested EntityMetadata,
            // since the host EntityMetadata is already cached.
            this.nested = new EntityMetadata(clazz);
            this.tableName = BuildHelper.defaultTableName(clazz);
        } else {
            this.tableName = BuildHelper.resolveTableName(viewClass);
        }
        this.joinConditions = resolveJoinConditions(viewClass);
        this.columnsForSelect = buildViewColumns(viewClass);
        this.groupByColumns = resolveGroupByColumns(viewClass);
        this.groupBySql = buildGroupBySql(groupByColumns);
        this.withViews = collectWithViews(viewClass);
        this.domainPathFields = ColumnUtil.resolveDomainPathFields(viewClass);
    }

    private List<View> collectWithViews(Class<?> entityClass) {
        ComplexView anno = entityClass.getAnnotation(ComplexView.class);
        if (anno == null) {
            return List.of();
        }
        return Arrays.stream(anno.value()).filter(view -> !view.with().isBlank()).toList();
    }

    public static List<String> resolveEntityRelations(Class<?>[] viewClasses) {
        List<ViewIndex> viewIndices = Arrays.stream(viewClasses).map(ViewIndex::new).toList();
        return resolveEntityRelations(viewIndices);
    }

    public static List<String> resolveEntityRelations(View[] views) {
        List<ViewIndex> viewIndices = Arrays.stream(views).map(ViewIndex::new).toList();
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

    public static EntityMetadata build(Class<?> viewClass) {
        return holder.computeIfAbsent(viewClass, EntityMetadata::new);
    }

    public static String buildViewColumns(Class<?> viewClass) {
        return ColumnUtil.filterFields(viewClass, ColumnUtil::filterForView)
                         .map(EntityMetadata::selectAs)
                         .collect(Collectors.joining(SEPARATOR));
    }

    public static String selectAs(Field field) {
        String columnName = resolveColumn(field);
        Dialect dialect = GlobalConfiguration.dialect();
        String fieldName = dialect.wrapLabel(field.getName());
        return columnName.equalsIgnoreCase(fieldName) || field.isAnnotationPresent(NoLabel.class)
                ? columnName : columnName + " AS " + fieldName;
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
        columnName = ColumnUtil.convertColumn(columnName);
        columnName = GlobalConfiguration.dialect().wrapLabel(columnName);
        if (aggregationPrefix != AggregationPrefix.NONE) {
            columnName = aggregationPrefix.getName() + "(" + columnName + ")";
        }
        return columnName;
    }

    static String resolveGroupByColumns(Class<?> viewClass) {
        return ColumnUtil.filterFields(viewClass, field -> field.isAnnotationPresent(GroupBy.class))
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
