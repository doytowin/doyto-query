package win.doyto.query.sql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.util.ColumnUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Pattern;

import static win.doyto.query.core.QuerySuffix.isValidValue;
import static win.doyto.query.sql.Constant.*;
import static win.doyto.query.util.CommonUtil.readFieldGetter;

/**
 * BuildHelper
 *
 * @author f0rb on 2021-02-16
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BuildHelper {
    private static final Pattern PTN_SORT = Pattern.compile(",(asc|desc)", Pattern.CASE_INSENSITIVE);

    static String buildStart(String[] columns, String from) {
        return Constant.SELECT + StringUtils.join(columns, SEPARATOR) + FROM + from;
    }

    public static String buildWhere(DoytoQuery query, List<Object> argList) {
        Field[] fields = ColumnUtil.initFields(query.getClass(), FieldProcessor::init);
        StringJoiner whereJoiner = new StringJoiner(" AND ", fields.length);
        for (Field field : fields) {
            Object value = readFieldGetter(field, query);
            if (isValidValue(value, field)) {
                String and = FieldProcessor.execute(field, argList, value);
                if (and != null) {
                    whereJoiner.append(and);
                }
            }
        }
        if (whereJoiner.isEmpty()) {
            return "";
        }
        return WHERE + whereJoiner;
    }

    static String buildOrderBy(DoytoQuery pageQuery) {
        if (pageQuery.getSort() == null) {
            return "";
        }
        return " ORDER BY " + PTN_SORT.matcher(pageQuery.getSort()).replaceAll(" $1").replace(";", SEPARATOR);
    }

    static String buildPaging(String sql, DoytoQuery pageQuery) {
        if (pageQuery.needPaging()) {
            sql = GlobalConfiguration.dialect().buildPageSql(sql, pageQuery.getPageSize(), pageQuery.calcOffset());
        }
        return sql;
    }

}
