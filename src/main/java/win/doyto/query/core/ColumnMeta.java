package win.doyto.query.core;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static win.doyto.query.core.GlobalConfiguration.convertColumn;

/**
 * ColumnMeta
 *
 * @author f0rb
 */
@AllArgsConstructor
class ColumnMeta {
    final String fieldName;
    final Object value;
    final List<Object> argList;

    String defaultSql(QuerySuffix querySuffix) {
        return defaultSql(querySuffix, getEx(argList, fieldName));
    }

    static String getEx(List<Object> argList, String fieldName) {
        return argList != null ? "?" : "#{" + fieldName + "}";
    }

    String defaultSql(QuerySuffix querySuffix, String ex) {
        String columnName = querySuffix.resolveColumnName(fieldName);
        if (columnName.contains("Or")) {
            LinkedList<String> objects = new LinkedList<>();
            for (String or : splitByOr(columnName)) {
                objects.add(convertColumn(camelize(or)) + SPACE + querySuffix.getOp() + SPACE + ex);
                appendArgs(value, argList);
            }
            return "(" + StringUtils.join(objects, " OR ") + ")";
        }

        appendArgs(value, argList);
        return convertColumn(columnName) + SPACE + querySuffix.getOp() + SPACE + ex;
    }

    static String camelize(String or) {
        return or.substring(0, 1).toLowerCase() + or.substring(1);
    }

    static String[] splitByOr(String columnName) {
        return columnName.split("Or(?=[A-Z])");
    }

    @SuppressWarnings("unchecked")
    private static void appendArgs(Object value, List<Object> argList) {
        if (argList != null) {
            if (value instanceof Collection) {
                argList.addAll((Collection<Object>) value);
            } else {
                argList.add(value);
            }
        }
    }

}
