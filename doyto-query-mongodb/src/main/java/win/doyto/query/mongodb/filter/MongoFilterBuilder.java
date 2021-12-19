package win.doyto.query.mongodb.filter;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.QuerySuffix;
import win.doyto.query.entity.Persistable;
import win.doyto.query.util.ColumnUtil;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.regex;
import static com.mongodb.client.model.Sorts.*;
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

    static {
        suffixFuncMap = new EnumMap<>(QuerySuffix.class);
        suffixFuncMap.put(Eq, Filters::eq);
        suffixFuncMap.put(Contain, (s, v) -> regex(s, v.toString()));
        suffixFuncMap.put(Lt, Filters::lt);
        suffixFuncMap.put(Le, Filters::lte);
        suffixFuncMap.put(Gt, Filters::gt);
        suffixFuncMap.put(Ge, Filters::gte);
        suffixFuncMap.put(In, Filters::in);
        suffixFuncMap.put(NotIn, Filters::nin);
        suffixFuncMap.put(Not, Filters::ne);
        suffixFuncMap.put(Near, MongoGeoFilters::near);
        suffixFuncMap.put(NearSphere, MongoGeoFilters::nearSphere);
        suffixFuncMap.put(Center, MongoGeoFilters::withinCenter);
        suffixFuncMap.put(CenterSphere, MongoGeoFilters::withinCenterSphere);
        suffixFuncMap.put(Box, MongoGeoFilters::withinBox);
        suffixFuncMap.put(Py, MongoGeoFilters::withinPolygon);
        suffixFuncMap.put(Within, MongoGeoFilters::within);
    }

    public static Bson buildFilter(Object query) {
        List<Bson> filters = new ArrayList<>();
        buildFilter(query, "", filters);
        switch (filters.size()) {
            case 0:
                return new Document();
            case 1:
                return filters.get(0);
            default:
                return and(filters);
        }
    }

    private static void buildFilter(Object query, String prefix, List<Bson> filters) {
        prefix = StringUtils.isEmpty(prefix) ? "" : prefix + ".";
        Field[] fields = ColumnUtil.initFields(query.getClass());
        for (Field field : fields) {
            Object value = CommonUtil.readFieldGetter(field, query);
            if (isValidValue(value, field)) {
                String newPrefix = prefix + field.getName();
                if (value instanceof DoytoQuery) {
                    buildFilter(value, newPrefix, filters);
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

    private static Bson resolveFilter(String fieldName, Object value) {
        QuerySuffix querySuffix = resolve(fieldName);
        String columnName = querySuffix.resolveColumnName(fieldName);
        return suffixFuncMap.getOrDefault(querySuffix, Filters::eq).apply(columnName, value);
    }

    public static Bson buildUpdates(Object target) {
        List<Bson> updates = new ArrayList<>();
        buildUpdates(target, "", updates);
        return Updates.combine(updates);
    }

    private static void buildUpdates(Object entity, String prefix, List<Bson> updates) {
        prefix = StringUtils.isEmpty(prefix) ? "" : prefix + ".";
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
        List<Bson> sortList = new ArrayList<>();
        Matcher matcher = SORT_PTN.matcher(sort.toLowerCase());
        while (matcher.find()) {
            String filedName = matcher.group(1);
            String direction = matcher.group(2);
            boolean isDesc = StringUtils.equals(direction, ",desc");
            sortList.add(isDesc ? descending(filedName) : ascending(filedName));
        }
        return orderBy(sortList);
    }
}
