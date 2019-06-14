package win.doyto.query.core;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static win.doyto.query.core.CommonUtil.convertColumn;
import static win.doyto.query.core.CommonUtil.wrapWithParenthesis;
import static win.doyto.query.core.Constant.REPLACE_HOLDER;
import static win.doyto.query.core.Constant.SPACE;

/**
 * ColumnMeta
 *
 * @author f0rb
 */
@AllArgsConstructor
final class ColumnMeta {
    final String fieldName;
    final Object value;
    final List<Object> argList;

    final String defaultSql(QuerySuffix querySuffix) {
        return defaultSql(querySuffix, querySuffix.getEx(value));
    }

    final String defaultSql(QuerySuffix querySuffix, String ex) {
        if (querySuffix.getOp().contains("LIKE") && value instanceof String && StringUtils.isBlank((String) value)) {
            return null;
        }
        if (!ex.isEmpty()) {
            ex = SPACE + ex;
        }
        String columnName = querySuffix.resolveColumnName(fieldName);
        if (columnName.contains("Or")) {
            int indexOfDot = columnName.indexOf('.') + 1;
            String alias = "";
            if (indexOfDot > 0) {
                alias = columnName.substring(0, indexOfDot);
                columnName = columnName.substring(indexOfDot);
            }
            String[] ors = CommonUtil.splitByOr(columnName);
            List<String> columns = new ArrayList<>(ors.length);
            for (String or : ors) {
                columns.add(alias + convertColumn(CommonUtil.camelize(or)) + SPACE + querySuffix.getOp() + ex);
                appendArgs(ex, value, argList);
            }
            return wrapWithParenthesis(StringUtils.join(columns, " OR "));
        }

        appendArgs(ex, value, argList);
        return convertColumn(columnName) + SPACE + querySuffix.getOp() + ex;
    }

    @SuppressWarnings("unchecked")
    private static void appendArgs(String ex, Object value, List<Object> argList) {
        if (value instanceof Collection) {
            argList.addAll((Collection<Object>) value);
        } else if (ex.contains(REPLACE_HOLDER)) {
            argList.add(value);
        }
    }

}
