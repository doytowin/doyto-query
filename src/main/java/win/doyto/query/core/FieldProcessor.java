package win.doyto.query.core;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static win.doyto.query.core.CommonUtil.wrapWithParenthesis;
import static win.doyto.query.core.Constant.*;

/**
 * FieldProcessor
 *
 * @author f0rb on 2019-06-04
 */
final class FieldProcessor {

    private static final Map<Field, Processor> FIELD_PROCESSOR_MAP = new ConcurrentHashMap<>();

    static String resolvedNestedQueries(List<Object> argList, Object value, NestedQueries nestedQueries, String fieldName, boolean fieldTypeNotPrimitiveBoolean) {
        return nestedQueries.column() +
            resolvedNestedQueries(nestedQueries) +
            QuerySuffix.buildWhereSql(argList, value, fieldName, fieldTypeNotPrimitiveBoolean) +
            StringUtils.repeat(')', nestedQueries.value().length);
    }

    private static String resolvedNestedQueries(NestedQueries nestedQueries) {
        StringBuilder subquery = new StringBuilder();
        String lastOp = nestedQueries.op();
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
        return subquery.toString();
    }

    private static String getNestedQuery(NestedQuery nestedQuery) {
        return SELECT +
            nestedQuery.left() +
            FROM +
            nestedQuery.table() +
            (nestedQuery.extra().isEmpty() ? EMPTY : SPACE + nestedQuery.extra());
    }

    @SuppressWarnings("unchecked")
    static String resolvedSubQuery(List<Object> argList, Object value, SubQuery subQuery, String fieldName, boolean fieldTypeNotPrimitiveBoolean) {
        String clause = SELECT + subQuery.left() + FROM + subQuery.table() +
            QuerySuffix.buildWhereSql(argList, value, fieldName, fieldTypeNotPrimitiveBoolean);
        return subQuery.column() + SPACE + subQuery.op() + SPACE + wrapWithParenthesis(clause);
    }

    static void init(Field field) {
        boolean fieldTypeNotPrimitiveBoolean = !field.getType().isAssignableFrom(boolean.class);
        String fieldName = field.getName();
        if (field.isAnnotationPresent(QueryField.class)) {
            String andSQL = field.getAnnotation(QueryField.class).and();
            FIELD_PROCESSOR_MAP.put(field, (argList, value) -> {
                IntStream.range(0, StringUtils.countMatches(andSQL, REPLACE_HOLDER)).mapToObj(i -> value).forEach(argList::add);
                return andSQL;
            });
        } else if (field.isAnnotationPresent(SubQuery.class)) {
            SubQuery subQuery = field.getAnnotation(SubQuery.class);
            FIELD_PROCESSOR_MAP.put(field, (argList, value) -> resolvedSubQuery(argList, value, subQuery, fieldName, fieldTypeNotPrimitiveBoolean));
        } else if (field.isAnnotationPresent(NestedQueries.class)) {
            NestedQueries nestedQueries = field.getAnnotation(NestedQueries.class);
            FIELD_PROCESSOR_MAP.put(field, (argList, value) -> resolvedNestedQueries(argList, value, nestedQueries, fieldName, fieldTypeNotPrimitiveBoolean));
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
