package win.doyto.query.core;

import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * QuerySuffix
 *
 * @author f0rb
 * @date 2019-05-16
 */
@SuppressWarnings("squid:S00115")
@Getter
enum QuerySuffix {
    Like, NotIn("NOT IN"), In, Gt(">"), Ge(">="), Lt("<"), Le("<="), NONE("=");

    QuerySuffix() {
        this.op = name().toUpperCase();
    }

    QuerySuffix(String op) {
        this.op = op;
    }

    private final String op;

    static final Pattern SUFFIX_PTN;

    private static final Map<QuerySuffix, Function<ColumnMeta, String>> sqlFuncMap = new EnumMap<>(QuerySuffix.class);

    static {
        LinkedList<String> suffixList = Arrays.stream(QuerySuffix.values()).filter(querySuffix -> querySuffix != NONE).map(Enum::name).collect(Collectors.toCollection(LinkedList::new));
        String suffixPtn = StringUtils.join(suffixList, "|");
        SUFFIX_PTN = Pattern.compile("(" + suffixPtn + ")$");

        for (QuerySuffix querySuffix : QuerySuffix.values()) {
            sqlFuncMap.put(querySuffix, columnMeta -> columnMeta.defaultSql(querySuffix));
        }

        sqlFuncMap.put(QuerySuffix.In, columnMeta -> buildSqlForCollection(columnMeta, QuerySuffix.In));
        sqlFuncMap.put(QuerySuffix.NotIn, columnMeta -> buildSqlForCollection(columnMeta, QuerySuffix.NotIn));
    }

    static String buildAndSql(String fieldName, @NonNull Object value, List<Object> argList) {
        QuerySuffix querySuffix = QuerySuffix.NONE;
        Matcher matcher = SUFFIX_PTN.matcher(fieldName);
        if (matcher.find()) {
            querySuffix = QuerySuffix.valueOf(matcher.group());
        }
        return sqlFuncMap.get(querySuffix).apply(new ColumnMeta(fieldName, value, argList));
    }

    private static String buildSqlForCollection(ColumnMeta columnMeta, QuerySuffix querySuffix) {
        String ex = "(null)";
        Collection collection = (Collection) columnMeta.value;
        if (!collection.isEmpty()) {
            List<Object> inList = IntStream.range(0, collection.size()).
                mapToObj(i -> columnMeta.argList != null ? "?" : String.format("#{%s[%d]}", columnMeta.fieldName, i)).collect(Collectors.toList());
            ex = "(" + StringUtils.join(inList, ", ") + ")";
        }
        return columnMeta.defaultSql(querySuffix, ex);
    }

}
