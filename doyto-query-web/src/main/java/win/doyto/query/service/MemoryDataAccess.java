/*
 * Copyright © 2019-2025 DoytoWin, Inc.
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

package win.doyto.query.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.annotation.GeneratedValue;
import win.doyto.query.annotation.Id;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.*;
import win.doyto.query.entity.Persistable;
import win.doyto.query.util.BeanUtil;
import win.doyto.query.util.ColumnUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static win.doyto.query.core.QuerySuffix.isValidValue;
import static win.doyto.query.core.QuerySuffix.resolve;
import static win.doyto.query.util.CommonUtil.*;

/**
 * MemoryDataAccess
 *
 * @author f0rb
 */
@Slf4j
@SuppressWarnings({"unchecked", "java:S3740"})
class MemoryDataAccess<E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery> implements DataAccess<E, I, Q> {
    protected static final Map<Class<?>, Map<?, ?>> tableMap = new ConcurrentHashMap<>();

    protected final Map<I, E> entitiesMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();
    private final List<Field> fields;
    private final Field idField;
    private final Class<I> idClass;

    public MemoryDataAccess(Class<E> entityClass) {
        tableMap.put(entityClass, entitiesMap);

        // init fields
        fields = ColumnUtil.getColumnFieldsFrom(entityClass);
        Field[] idFields = FieldUtils.getFieldsWithAnnotation(entityClass, Id.class);
        if (idFields.length == 1 && idFields[0].isAnnotationPresent(GeneratedValue.class)) {
            idField = idFields[0];
            idClass = BeanUtil.getIdClass(entityClass, idField.getName());
        } else {
            idField = null;
            idClass = null;
        }
    }

    protected void generateNewId(E entity) {
        try {
            Object newId = chooseIdValue(idGenerator.incrementAndGet(), idClass);
            writeField(idField, entity, newId);
        } catch (Exception e) {
            log.warn("写入id失败: {} - {}", entity.getClass(), e.getMessage());
        }
    }

    private Object chooseIdValue(Long newId, Class<?> type) {
        Object t = newId;
        if (type.isAssignableFrom(Integer.class)) {
            t = newId.intValue();
        }
        return t;
    }

    @Override
    public E get(IdWrapper<I> idWrapper) {
        return SerializationUtils.clone(entitiesMap.get(idWrapper.getId()));
    }

    @Override
    public List<I> queryIds(Q query) {
        return queryColumns(query, idClass, "id");
    }

    @Override
    public void create(E e) {
        if (idField != null) {
            generateNewId(e);
        }
        entitiesMap.put(e.getId(), e);
    }

    @Override
    public int update(E e) {
        if (!entitiesMap.containsKey(e.getId())) {
            return 0;
        }
        entitiesMap.put(e.getId(), e);
        return 1;
    }

    @Override
    public int patch(E patch) {
        E origin = entitiesMap.get(patch.getId());
        if (origin == null) {
            return 0;
        }

        for (Field field : fields) {
            Object value = readField(field, patch);
            if (value != null) {
                writeField(field, origin, value);
            }
        }
        return 1;
    }

    @Override
    public int patch(E p, Q q) {
        List<E> list = query(q);
        for (E origin : list) {
            p.setId(origin.getId());
            patch(p);
        }
        return list.size();
    }

    @Override
    public int delete(IdWrapper<I> idWrapper) {
        return entitiesMap.remove(idWrapper.getId()) == null ? 0 : 1;
    }

    @Override
    public int delete(Q query) {
        List<E> list = query(query);
        list.stream().map(Persistable::getId).forEach(entitiesMap::remove);
        return list.size();
    }

    /**
     * 根据Query对象筛选符合条件的Entity对象
     *
     * @param query  Query
     * @param entity Entity
     * @return true, Entity符合条件需要保留; false, Entity不符合条件需要过滤掉
     */
    protected boolean filterByQuery(Q query, E entity) {
        for (Field field : query.getClass().getDeclaredFields()) {
            if (supportFilter(field)) {
                Object value = readField(field, query);
                if (isValidValue(value, field) && shouldDiscard(entity, field.getName(), value)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean supportFilter(Field field) {
        return ColumnUtil.filterForEntity(field);
    }

    protected boolean shouldDiscard(E entity, String queryFieldName, Object queryFieldValue) {
        if (containsOr(queryFieldName)) {
            boolean result = true;
            for (String fieldName : splitByOr(queryFieldName)) {
                result &= shouldDiscard(entity, fieldName, queryFieldValue);
            }
            return result;
        }
        QuerySuffix querySuffix = resolve(queryFieldName);
        String columnName = querySuffix.resolveColumnName(queryFieldName);
        Matcher matcher = FilterExecutor.get(querySuffix);

        Object entityFieldValue = readField(entity, columnName);
        return !matcher.match(queryFieldValue, entityFieldValue);
    }

    @Override
    public List<E> query(Q query) {
        @SuppressWarnings("java:S6204")
        List<E> queryList = entitiesMap
                .values().stream()
                .filter(item -> filterByQuery(query, item))
                .collect(Collectors.toList());

        if (query.getSort() != null) {
            doSort(queryList, query.getSort());
        }
        if (query.needPaging()) {
            queryList = truncateByPaging(queryList, query);
        }

        return queryList;
    }

    private List<E> truncateByPaging(List<E> queryList, Q query) {
        int from = GlobalConfiguration.calcOffset(query);
        int end = Math.min(queryList.size(), from + query.getPageSize());
        if (from <= end) {
            queryList = queryList.subList(from, end);
        }
        return queryList;
    }

    @Override
    public <V> List<V> queryColumns(Q q, Class<V> classV, String... columns) {
        List<E> entities = query(q);
        List<V> objects = new ArrayList<>(entities.size());
        if (columns.length == 1) {
            return entities.stream().map(entity -> (V) readField(entity, columns[0])).toList();
        } else {
            for (E e : entities) {
                objects.add(BeanUtil.convertTo(e, classV));
            }
        }
        return objects;
    }

    protected void doSort(List<E> queryList, String sort) {
        String[] orders = StringUtils.split(sort, ";");
        for (int i = orders.length - 1; i >= 0; i--) {
            String order = orders[i];
            queryList.sort((o1, o2) -> {
                String[] pd = StringUtils.split(order, ",");
                String property = toCamelCase(pd[0]);
                Comparable<Object> c1 = (Comparable<Object>) readField(o1, property);
                Object c2 = readField(o2, property);
                int ret = c1.compareTo(c2);
                return "asc".equalsIgnoreCase(pd[1]) ? ret : -ret;
            });
        }
    }

    @Override
    public long count(Q query) {
        return entitiesMap.values().stream().filter(item -> filterByQuery(query, item)).count();
    }

    @Override
    public PageList<E> page(Q query) {
        query.forcePaging();
        return new PageList<>(query(query), count(query));
    }

}
