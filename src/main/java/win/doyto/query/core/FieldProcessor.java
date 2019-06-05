package win.doyto.query.core;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static win.doyto.query.core.CommonUtil.*;
import static win.doyto.query.core.Constant.*;

/**
 * FieldProcessor
 *
 * @author f0rb on 2019-06-04
 */
final class FieldProcessor {

    private static final Map<Field, Processor> FIELD_PROCESSOR_MAP = new ConcurrentHashMap<>();

    static String resolvedNestedQuery(List<Object> argList, Object value, NestedQueries nestedQueries) {
        String andSQL;
        String subquery = getNestedQueries(nestedQueries);
        andSQL = (nestedQueries.column() + subquery + (nestedQueries.right().isEmpty() ? EMPTY : " = " + REPLACE_HOLDER ))
            + StringUtils.repeat(')', nestedQueries.value().length);
        argList.add(value);
        return andSQL;
    }

    private static String getNestedQueries(NestedQueries nestedQueries) {
        StringBuilder subquery = new StringBuilder();
        String lastOp = IN;
        NestedQuery[] value = nestedQueries.value();
        NestedQuery nestedQuery = value[0];
        subquery.append(SPACE).append(lastOp).append(" (").append(getNestedQuery(nestedQuery));
        lastOp = nestedQuery.op();
        for (int i = 1, valueLength = value.length; i < valueLength; i++) {
            nestedQuery = value[i];
            subquery.append(WHERE).append(nestedQuery.left());
            subquery.append(SPACE).append(lastOp).append(" (").append(getNestedQuery(nestedQuery));
            lastOp = nestedQuery.op();
        }
        subquery.append((nestedQueries.right().isEmpty() ? EMPTY : WHERE + nestedQueries.right()));
        return subquery.toString();
    }

    private static String getNestedQuery(NestedQuery nestedQuery) {
        return SELECT +
            nestedQuery.left() +
            FROM +
            nestedQuery.table() +
            (nestedQuery.extra().isEmpty() ? EMPTY : SPACE) +
            nestedQuery.extra();
    }

    @SuppressWarnings("unchecked")
    static String resolvedSubQuery(List<Object> argList, Object value, SubQuery subQuery, String fieldName) {
        StringBuilder clauseBuilder = new StringBuilder()
            .append(SELECT).append(subQuery.left()).append(FROM).append(subQuery.table())
            .append(subQuery.extra().isEmpty() ? EMPTY : SPACE).append(subQuery.extra());

        if (!subQuery.ignoreField()) {
            clauseBuilder.append(WHERE);
            boolean noColumn = subQuery.right().isEmpty();
            if (!noColumn) {
                clauseBuilder.append(subQuery.right());
            }
            if (value instanceof Collection) {
                if (noColumn) {
                    String singular = StringUtils.removeEnd(StringUtils.removeEnd(fieldName, "s"), "List");
                    clauseBuilder.append(convertColumn(singular));
                }
                Collection collection = (Collection) value;
                clauseBuilder.append(" IN ");
                clauseBuilder.append(generateReplaceHoldersForCollection(collection.size()));
                argList.addAll(collection);
            } else {
                if (noColumn) {
                    clauseBuilder.append(convertColumn(fieldName));
                }
                clauseBuilder.append(EQUAL).append(REPLACE_HOLDER);
                argList.add(value);
            }
        }
        return subQuery.column() + SPACE + subQuery.op() + SPACE + wrapWithParenthesis(clauseBuilder.toString());
    }

    static void init(Field field) {
        String fieldName = field.getName();
        if (field.isAnnotationPresent(QueryField.class)) {
            String andSQL = field.getAnnotation(QueryField.class).and();
            FIELD_PROCESSOR_MAP.put(field, (argList, value) -> {
                IntStream.range(0, StringUtils.countMatches(andSQL, REPLACE_HOLDER)).mapToObj(i -> value).forEach(argList::add);
                return andSQL;
            });
        } else if (field.isAnnotationPresent(SubQuery.class)) {
            SubQuery subQuery = field.getAnnotation(SubQuery.class);
            FIELD_PROCESSOR_MAP.put(field, (argList, value) -> resolvedSubQuery(argList, value, subQuery, fieldName));
        } else if (field.isAnnotationPresent(NestedQueries.class)) {
            NestedQueries nestedQueries = field.getAnnotation(NestedQueries.class);
            FIELD_PROCESSOR_MAP.put(field, (argList, value) -> resolvedNestedQuery(argList, value, nestedQueries));
        } else {
            FIELD_PROCESSOR_MAP.put(field, (argList, value) -> QuerySuffix.buildAndSql(argList, value, fieldName));
        }
    }

    static String execute(List<Object> argList, Field field, Object value) {
        return FIELD_PROCESSOR_MAP.get(field).process(argList, value);
    }

    private interface Processor {
        String process(List<Object> args, Object value);
    }

}
