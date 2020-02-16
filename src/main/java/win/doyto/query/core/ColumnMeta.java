package win.doyto.query.core;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.annotation.Enumerated;

import java.util.Collection;
import java.util.List;
import javax.persistence.EnumType;

import static win.doyto.query.core.CommonUtil.convertColumn;
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

    final String defaultSql(QuerySuffix querySuffix, String ex) {
        if (querySuffix.getOp().contains("LIKE") && value instanceof String && StringUtils.isBlank((String) value)) {
            return null;
        }
        if (!ex.isEmpty()) {
            ex = SPACE + ex;
        }
        String columnName = querySuffix.resolveColumnName(fieldName);
        appendArgs(ex, value, argList);
        return convertColumn(columnName) + SPACE + querySuffix.getOp() + ex;
    }

    @SuppressWarnings("unchecked")
    private static void appendArgs(String ex, Object value, List<Object> argList) {
        if (value instanceof Collection) {
            Collection<Object> collection = (Collection<Object>) value;
            if (collection.isEmpty()) {
                return;
            }
            Object next = collection.iterator().next();
            if (next instanceof Enum) {
                Enumerated enumerated = next.getClass().getAnnotation(Enumerated.class);
                boolean isString = enumerated != null && enumerated.value() == EnumType.STRING;
                collection.stream()
                          .map(element -> isString ? element.toString() : ((Enum) element).ordinal())
                          .forEach(argList::add);
            } else {
                argList.addAll(collection);
            }
        } else if (ex.contains(REPLACE_HOLDER)) {
            argList.add(value);
        }
    }

}
