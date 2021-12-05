package win.doyto.query.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.config.GlobalConfiguration;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static win.doyto.query.core.CommonUtil.readFieldGetter;
import static win.doyto.query.core.Constant.*;
import static win.doyto.query.core.QuerySuffix.isValidValue;

/**
 * BuildHelper
 *
 * @author f0rb on 2021-02-16
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BuildHelper {
    private static final Pattern PTN_SORT = Pattern.compile(",(asc|desc)", Pattern.CASE_INSENSITIVE);
    private static final Map<Class<?>, Field[]> classFieldsMap = new ConcurrentHashMap<>();

    static String buildStart(String[] columns, String from) {
        return Constant.SELECT + StringUtils.join(columns, SEPARATOR) + FROM + from;
    }

    public static String buildWhere(Pageable query, List<Object> argList) {
        Field[] fields = initFields(query.getClass());
        StringJoiner whereJoiner = new StringJoiner(" AND ", fields.length);
        for (Field field : fields) {
            Object value = readFieldGetter(field, query);
            if (isValidValue(value, field)) {
                String and = FieldProcessor.execute(field, argList, value);
                whereJoiner.append(and);
            }
        }
        if (whereJoiner.isEmpty()) {
            return "";
        }
        return WHERE + whereJoiner;
    }

    public static Field[] initFields(Class<?> queryClass) {
        classFieldsMap.computeIfAbsent(queryClass, c -> {
            Field[] fields = Arrays.stream(c.getDeclaredFields()).filter(CommonUtil::fieldFilter).toArray(Field[]::new);
            Arrays.stream(fields).forEach(FieldProcessor::init);
            return fields;
        });
        return classFieldsMap.get(queryClass);
    }

    static String buildOrderBy(Pageable pageQuery) {
        if (pageQuery.getSort() == null) {
            return "";
        }
        return " ORDER BY " + PTN_SORT.matcher(pageQuery.getSort()).replaceAll(" $1").replace(";", SEPARATOR);
    }

    static String buildPaging(String sql, Pageable pageQuery) {
        if (pageQuery.needPaging()) {
            sql = GlobalConfiguration.dialect().buildPageSql(sql, pageQuery.getPageSize(), pageQuery.calcOffset());
        }
        return sql;
    }

}
