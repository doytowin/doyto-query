package win.doyto.query.core;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import win.doyto.query.entity.Persistable;

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

/**
 * MongoFilterUtil
 *
 * @author f0rb on 2021-11-23
 */
@UtilityClass
public class MongoFilterUtil {

    private static final Map<QuerySuffix, BiFunction<String, Object, Bson>> suffixFuncMap;

    static {
        suffixFuncMap = new EnumMap<>(QuerySuffix.class);
        suffixFuncMap.put(QuerySuffix.Eq, Filters::eq);
        suffixFuncMap.put(QuerySuffix.Contain, (s, v) -> regex(s, v.toString()));
        suffixFuncMap.put(QuerySuffix.Lt, Filters::lt);
        suffixFuncMap.put(QuerySuffix.Le, Filters::lte);
        suffixFuncMap.put(QuerySuffix.Gt, Filters::gt);
        suffixFuncMap.put(QuerySuffix.Ge, Filters::gte);
        suffixFuncMap.put(QuerySuffix.In, Filters::in);
        suffixFuncMap.put(QuerySuffix.NotIn, Filters::nin);
        suffixFuncMap.put(QuerySuffix.Not, Filters::ne);
    }

    @SneakyThrows
    public static Bson buildFilter(Object query) {
        List<Bson> filters = new ArrayList<>();
        buildFilter(query, "", filters);
        return filters.isEmpty() ? new Document() : and(filters);
    }

    private static void buildFilter(Object query, String prefix, List<Bson> filters) {
        prefix = StringUtils.isEmpty(prefix) ? "" : prefix + ".";
        Field[] fields = BuildHelper.initFields(query.getClass());
        for (Field field : fields) {
            Object value = CommonUtil.readFieldGetter(field, query);
            if (CommonUtil.isValidValue(value, field)) {
                if (value instanceof PageQuery) {
                    buildFilter(value, field.getName(), filters);
                } else {
                    filters.add(resolveFilter(prefix + field.getName(), value));
                }
            }
        }
    }

    private static Bson resolveFilter(String fieldName, Object value) {
        QuerySuffix querySuffix = QuerySuffix.resolve(fieldName);
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
        for (Field field : BuildHelper.initFields(target.getClass())) {
            Object value = CommonUtil.readFieldGetter(field, target);
            if (CommonUtil.isValidValue(value, field)) {
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
