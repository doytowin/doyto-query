package win.doyto.query.core;

import com.mongodb.client.model.Filters;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.regex;

/**
 * MongoFilterUtil
 *
 * @author f0rb on 2021-11-23
 */
public class MongoFilterUtil {

    private static final Map<QuerySuffix, BiFunction<String, Object, Bson>> suffixFuncMap;

    static {
        suffixFuncMap = new HashMap<>();
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
                if (value instanceof Query) {
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
}
