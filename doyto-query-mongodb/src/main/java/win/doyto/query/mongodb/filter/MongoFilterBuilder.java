package win.doyto.query.mongodb.filter;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import win.doyto.query.core.Pageable;
import win.doyto.query.core.QuerySuffix;
import win.doyto.query.entity.Persistable;
import win.doyto.query.mongodb.entity.BsonDeserializer;
import win.doyto.query.util.BeanUtil;
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
        suffixFuncMap.put(Py, MongoGeoFilters::polygon);
        suffixFuncMap.put(Within, MongoGeoFilters::within);

        BeanUtil.register(Bson.class, new BsonDeserializer());
    }

    @SneakyThrows
    public static Bson buildFilter(Object query) {
        List<Bson> filters = new ArrayList<>();
        buildFilter(query, "", filters);
        return filters.isEmpty() ? new Document() : and(filters);
    }

    private static void buildFilter(Object query, String prefix, List<Bson> filters) {
        prefix = StringUtils.isEmpty(prefix) ? "" : prefix + ".";
        Field[] fields = ColumnUtil.initFields(query.getClass());
        for (Field field : fields) {
            Object value = CommonUtil.readFieldGetter(field, query);
            if (isValidValue(value, field)) {
                String newPrefix = prefix + field.getName();
                if (value instanceof Pageable) {
                    buildFilter(value, newPrefix, filters);
                } else {
                    filters.add(resolveFilter(newPrefix, value));
                }
            } else if (value instanceof Bson) {
                String fieldName = field.getName();
                String column = resolve(fieldName).resolveColumnName(fieldName);
                filters.add(new Document(column, value));
            }
        }
    }

    private static Bson resolveFilter(String fieldName, Object value) {
        QuerySuffix querySuffix = resolve(fieldName);
        String columnName = querySuffix.resolveColumnName(fieldName);
        return suffixFuncMap
                .getOrDefault(querySuffix, Filters::eq)
                .apply(columnName, value);
    }

    public static Bson buildUpdates(Object target) {
        ArrayList<Bson> updates = new ArrayList<>();
        buildUpdates(target, "", updates);
        return Updates.combine(updates);
    }

    private static void buildUpdates(Object target, String prefix, List<Bson> updates) {
        prefix = StringUtils.isEmpty(prefix) ? "" : prefix + ".";
        for (Field field : ColumnUtil.initFields(target.getClass())) {
            Object value = CommonUtil.readFieldGetter(field, target);
            if (isValidValue(value, field)) {
                if (value instanceof Persistable) {
                    buildUpdates(value, prefix + field.getName(), updates);
                } else {
                    updates.add(Updates.set(prefix + field.getName(), value));
                }
            }
        }
    }

    public static Bson buildSort(String sort) {
        List<Bson> sortList = new ArrayList<>();
        Matcher matcher = Pattern.compile("(\\w+)(,asc|,desc)?").matcher(sort);
        while (matcher.find()) {
            String filedName = matcher.group(1);
            if (StringUtils.contains(matcher.group(2), "desc")) {
                sortList.add(descending(filedName));
            } else {
                sortList.add(ascending(filedName));
            }
        }
        return orderBy(sortList);
    }
}
