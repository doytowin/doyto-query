package win.doyto.query.sql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.annotation.NestedQueries;
import win.doyto.query.annotation.NestedQuery;
import win.doyto.query.annotation.QueryField;
import win.doyto.query.annotation.QueryTableAlias;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.Or;
import win.doyto.query.core.QuerySuffix;
import win.doyto.query.util.ColumnUtil;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static win.doyto.query.sql.Constant.*;

/**
 * FieldProcessor
 *
 * @author f0rb on 2019-06-04
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class FieldProcessor {

    private static final Map<Field, Processor> FIELD_PROCESSOR_MAP = new ConcurrentHashMap<>();
    public static final Processor EMPTY_PROCESSOR = ((argList, value) -> EMPTY);

    public static String execute(Field field, List<Object> argList, Object value) {
        return FIELD_PROCESSOR_MAP.get(field).process(argList, value);
    }

    public static void init(Field field) {
        Processor processor;
        if (Or.class.isAssignableFrom(field.getType())) {
            processor = initFieldMappedByOr(field);
        } else if (field.isAnnotationPresent(QueryTableAlias.class)) {
            processor = initFieldAnnotatedByQueryTableAlias(field);
        } else if (field.isAnnotationPresent(QueryField.class)) {
            processor = initFieldAnnotatedByQueryField(field);
        } else if (field.isAnnotationPresent(NestedQueries.class)) {
            processor = initFieldAnnotatedByNestedQueries(field);
        } else {
            processor = initCommonField(field);
        }
        FIELD_PROCESSOR_MAP.put(field, processor);
    }

    private static Processor initFieldMappedByOr(Field field) {
        Field[] fields = ColumnUtil.initFields(field.getType());
        Arrays.stream(fields).forEach(FieldProcessor::init);
        return (argList, value) -> {
            StringJoiner or = new StringJoiner(SPACE_OR, fields.length);
            for (Field subField : fields) {
                Object subValue = CommonUtil.readField(subField, value);
                if (QuerySuffix.isValidValue(subValue, subField)) {
                    String condition = execute(subField, argList, subValue);
                    or.append(condition);
                }
            }
            return CommonUtil.wrapWithParenthesis(or.toString());
        };
    }

    private static Processor initCommonField(Field field) {
        String fieldName = field.getName();
        return chooseProcessorForFieldWithOr(fieldName);
    }

    private static Processor initFieldAnnotatedByQueryTableAlias(Field field) {
        String fieldName = field.getName();
        String tableAlias = field.getAnnotation(QueryTableAlias.class).value();
        String fieldNameWithAlias = tableAlias + "." + fieldName;
        return chooseProcessorForFieldWithOr(fieldNameWithAlias);
    }

    private static Processor chooseProcessorForFieldWithOr(String fieldName) {
        if (CommonUtil.containsOr(fieldName)) {
            return (argList, value) -> SqlQuerySuffix.buildConditionForFieldContainsOr(fieldName, argList, value);
        } else {
            return (argList, value) -> SqlQuerySuffix.buildConditionForField(fieldName, argList, value);
        }
    }

    private static Processor initFieldAnnotatedByQueryField(Field field) {
        String andSQL = field.getAnnotation(QueryField.class).and();
        int holderCount = StringUtils.countMatches(andSQL, PLACE_HOLDER);
        return (argList, value) -> {
            for (int i = 0; i < holderCount; i++) {
                argList.add(value);
            }
            return andSQL;
        };
    }

    private static Processor initFieldAnnotatedByNestedQueries(Field field) {
        NestedQueries nestedQueries = field.getAnnotation(NestedQueries.class);
        Processor processor = chooseProcessorForNestedQuery(field);
        return (argList, value) -> resolvedNestedQueries(argList, value, nestedQueries, processor);
    }

    private static Processor chooseProcessorForNestedQuery(Field field) {
        Processor processor;
        Class<?> fieldType = field.getType();
        if (boolean.class.isAssignableFrom(fieldType)) {
            processor = EMPTY_PROCESSOR;
        } else if (DoytoQuery.class.isAssignableFrom(fieldType)) {
            processor = (argList, value) -> BuildHelper.buildWhere((DoytoQuery) value, argList);
        } else {
            String fieldName = field.getName();
            if (CommonUtil.containsOr(fieldName)) {
                processor = (argList, value) -> WHERE + SqlQuerySuffix.buildConditionForFieldContainsOr(fieldName, argList, value);
            } else {
                processor = (argList, value) -> WHERE + SqlQuerySuffix.buildConditionForField(fieldName, argList, value);
            }
        }
        return processor;
    }

    private static String resolvedNestedQueries(List<Object> argList, Object value, NestedQueries nestedQueries, Processor processor) {
        StringBuilder nestQuery = resolvedNestedQueries(nestedQueries);
        IntStream.range(0, StringUtils.countMatches(nestQuery, PLACE_HOLDER)).mapToObj(i -> value).forEach(argList::add);
        if (nestedQueries.appendWhere()) {
            nestQuery.append(processor.process(argList, value));
        }
        return nestedQueries.column() + nestQuery + StringUtils.repeat(')', nestedQueries.value().length);
    }

    private static StringBuilder resolvedNestedQueries(NestedQueries nestedQueries) {
        StringBuilder nestedQueryBuilder = new StringBuilder();
        String lastOp = nestedQueries.op();
        String lastWhere = nestedQueries.column();
        NestedQuery[] nestedQueryArr = nestedQueries.value();

        for (int i = 0; i < nestedQueryArr.length; i++) {
            NestedQuery nestedQuery = nestedQueryArr[i];
            if (i > 0) {
                nestedQueryBuilder.append(WHERE).append(StringUtils.defaultIfBlank(lastWhere, nestedQuery.select()));
            }
            nestedQueryBuilder.append(SPACE).append(lastOp).append(" (").append(getNestedQuery(nestedQuery));

            lastOp = nestedQuery.op();
            lastWhere = nestedQuery.where();
        }
        return nestedQueryBuilder;
    }

    private static String getNestedQuery(NestedQuery nestedQuery) {
        return SELECT +
                nestedQuery.select() +
                FROM +
                nestedQuery.from() +
                StringUtils.defaultIfBlank(SPACE + nestedQuery.extra(), EMPTY);
    }

    interface Processor {
        String process(List<Object> argList, Object value);
    }

}
