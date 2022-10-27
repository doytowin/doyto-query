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

package win.doyto.query.mongodb.filter;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.Or;
import win.doyto.query.core.QuerySuffix;
import win.doyto.query.entity.Persistable;
import win.doyto.query.util.ColumnUtil;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static win.doyto.query.core.QuerySuffix.*;

/**
 * MongoFilterUtil
 *
 * @author f0rb on 2021-11-23
 */
@UtilityClass
public class MongoFilterBuilder {

    private static final Map<QuerySuffix, BiFunction<String, Object, Bson>> suffixFuncMap;
    private static final Pattern SORT_PTN = Pattern.compile("(\\w+)(,asc|,desc)?");
    private static final Bson EMPTY_DOCUMENT = new EmptyBson();

    static {
        suffixFuncMap = new EnumMap<>(QuerySuffix.class);
        suffixFuncMap.put(Eq, Filters::eq);
        suffixFuncMap.put(Contain, (s, v) -> regex(s, v.toString()));
        suffixFuncMap.put(Lt, Filters::lt);
        suffixFuncMap.put(Le, Filters::lte);
        suffixFuncMap.put(Gt, Filters::gt);
        suffixFuncMap.put(Ge, Filters::gte);
        suffixFuncMap.put(In, (fieldName, values) -> Filters.in(fieldName, (Iterable<?>) values));
        suffixFuncMap.put(NotIn, (fieldName, values) -> Filters.nin(fieldName, (Iterable<?>) values));
        suffixFuncMap.put(Not, Filters::ne);
        suffixFuncMap.put(Near, MongoGeoFilters::near);
        suffixFuncMap.put(NearSphere, MongoGeoFilters::nearSphere);
        suffixFuncMap.put(Center, MongoGeoFilters::withinCenter);
        suffixFuncMap.put(CenterSphere, MongoGeoFilters::withinCenterSphere);
        suffixFuncMap.put(Box, MongoGeoFilters::withinBox);
        suffixFuncMap.put(Py, MongoGeoFilters::withinPolygon);
        suffixFuncMap.put(Within, MongoGeoFilters::withIn);
        suffixFuncMap.put(IntX, MongoGeoFilters::intersects);
    }

    public static Bson buildFilter(Object query) {
        return buildFilter(query, EMPTY);
    }

    public static Bson buildFilter(Object query, String prefix) {
        List<Bson> filters = new ArrayList<>();
        buildFilter(query, prefix, filters);
        switch (filters.size()) {
            case 0:
                return EMPTY_DOCUMENT;
            case 1:
                return filters.get(0);
            default:
                return and(filters);
        }
    }

    private static void buildFilter(Object query, String prefix, List<Bson> filters) {
        prefix = StringUtils.isEmpty(prefix) ? EMPTY : prefix + ".";
        Field[] fields = ColumnUtil.initFields(query.getClass());
        for (Field field : fields) {
            Object value = CommonUtil.readFieldGetter(field, query);
            if (isValidValue(value, field)) {
                String newPrefix = prefix + field.getName();
                if (value instanceof DoytoQuery) {
                    buildFilter(value, newPrefix, filters);
                } else if (value instanceof Or) {
                    buildOrFilter(value, filters);
                } else {
                    filters.add(resolveFilter(newPrefix, value));
                }
            } else if (value instanceof Bson) {
                // process Bson value directly
                String fieldName = field.getName();
                String column = resolve(fieldName).resolveColumnName(fieldName);
                filters.add(new Document(column, value));
            }
        }
    }

    private static void buildOrFilter(Object value, List<Bson> rootFilters) {
        List<Bson> filters = new ArrayList<>();
        buildFilter(value, EMPTY, filters);
        switch (filters.size()) {
            case 0:
                break;
            case 1:
                rootFilters.add(filters.get(0));
                break;
            default:
                rootFilters.add(or(filters));
        }
    }

    private static Bson resolveFilter(String fieldName, Object value) {
        QuerySuffix querySuffix = resolve(fieldName);
        String columnName = querySuffix.resolveColumnName(fieldName);
        return suffixFuncMap.getOrDefault(querySuffix, Filters::eq).apply(columnName, value);
    }

    public static Bson buildUpdates(Object target) {
        List<Bson> updates = new ArrayList<>();
        buildUpdates(target, EMPTY, updates);
        return Updates.combine(updates);
    }

    private static void buildUpdates(Object entity, String prefix, List<Bson> updates) {
        prefix = StringUtils.isEmpty(prefix) ? EMPTY : prefix + ".";
        Field[] fields = ColumnUtil.initFields(entity.getClass());
        for (Field field : fields) {
            Object value = CommonUtil.readFieldGetter(field, entity);
            if (isValidValue(value, field)) {
                String newPrefix = prefix + field.getName();
                if (value instanceof Persistable) {
                    buildUpdates(value, newPrefix, updates);
                } else {
                    updates.add(Updates.set(newPrefix, value));
                }
            }
        }
    }

    public static Bson buildSort(String sort) {
        return buildSort(sort, Collections.emptySet());
    }

    public static Bson buildSort(String sort, Set<String> groupColumns) {
        List<Bson> sortList = new ArrayList<>();
        Matcher matcher = SORT_PTN.matcher(sort.toLowerCase());
        while (matcher.find()) {
            String filedName = matcher.group(1);
            if (groupColumns.contains(filedName)) {
                filedName = "_id." + filedName;
            }
            String direction = matcher.group(2);
            boolean isDesc = StringUtils.equals(direction, ",desc");
            sortList.add(isDesc ? descending(filedName) : ascending(filedName));
        }
        return orderBy(sortList);
    }
}
