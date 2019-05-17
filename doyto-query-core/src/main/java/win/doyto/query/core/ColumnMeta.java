package win.doyto.query.core;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * ColumnMeta
 *
 * @author f0rb
 * @date 2019-05-17
 */
@AllArgsConstructor
class ColumnMeta {
    final String fieldName;
    final Object value;
    final List<Object> argList;

    String defaultSql(QuerySuffix querySuffix) {
        return defaultSql(querySuffix, argList != null ? "?" : "#{" + fieldName + "}");
    }

    String defaultSql(QuerySuffix querySuffix, String ex) {
        String columnName = resolveColumnName(fieldName, querySuffix.name());
        if (columnName.contains("Or")) {
            LinkedList<String> objects = new LinkedList<>();
            for (String or : splitByOr(columnName)) {
                objects.add(or.substring(0, 1).toLowerCase() + or.substring(1) + " " + querySuffix.getOp() + " " + ex);
                appendArgs(value, argList);
            }
            return "(" + StringUtils.join(objects, " OR ") + ")";
        }

        appendArgs(value, argList);
        return columnName + " " + querySuffix.getOp() + " " + ex;
    }

    static String[] splitByOr(String columnName) {
        return columnName.split("Or(?=[A-Z])");
    }

    private static String resolveColumnName(String fieldName, String suffix) {
        return fieldName.endsWith(suffix) ? fieldName.substring(0, fieldName.length() - suffix.length()) : fieldName;
    }

    private static void appendArgs(Object value, List<Object> argList) {
        if (argList != null) {
            if (value instanceof Collection) {
                argList.addAll((Collection) value);
            } else {
                argList.add(value);
            }
        }
    }

}
