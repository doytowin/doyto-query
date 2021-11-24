package win.doyto.query.core;

import lombok.SneakyThrows;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

/**
 * MongoFilterUtil
 *
 * @author f0rb on 2021-11-23
 */
public class MongoFilterUtil {
    @SneakyThrows
    public static Bson buildFilter(Object query) {
        List<Bson> filters = new ArrayList<>();
        Field[] fields = BuildHelper.initFields(query.getClass());
        for (Field field : fields) {
            Object value = CommonUtil.readFieldGetter(field, query);
            if (CommonUtil.isValidValue(value, field)) {
                filters.add(resolveFilter(field.getName(), value));
            }
        }
        return filters.isEmpty() ? new Document() : and(filters);
    }

    private static Bson resolveFilter(String fieldName, Object value) {
        QuerySuffix querySuffix = QuerySuffix.resolve(fieldName);
        String columnName = querySuffix.resolveColumnName(fieldName);
        if (QuerySuffix.Contain == querySuffix) {
            return regex(columnName, value.toString());
        }
        return eq(fieldName, value);
    }
}
