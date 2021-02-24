package win.doyto.query.core;

import org.apache.commons.lang3.SerializationUtils;
import win.doyto.query.annotation.Joins;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.Table;

import static win.doyto.query.core.BuildHelper.*;
import static win.doyto.query.core.CommonUtil.*;
import static win.doyto.query.core.Constant.*;

/**
 * JoinQueryBuilder
 *
 * @author f0rb on 2019-06-09
 */
public class JoinQueryBuilder {

    private static final Pattern PTN_PLACE_HOLDER = Pattern.compile("#\\{(\\w+)}");

    protected final String tableName;
    protected String[] columnsForSelect;
    private Class<?> entityClass;
    private String joinSql;

    public JoinQueryBuilder(Class<?> entityClass) {
        this.entityClass = entityClass;
        tableName = entityClass.getAnnotation(Table.class).name();
        columnsForSelect = Arrays
                .stream(entityClass.getDeclaredFields())
                .filter(CommonUtil::fieldFilter)
                .map(CommonUtil::selectAs)
                .toArray(String[]::new);
        joinSql = buildJoinSql();
    }

    private String buildJoinSql() {
        Joins.Join[] joins = entityClass.getAnnotation(Joins.class).value();
        StringJoiner joiner = new StringJoiner(SPACE, joins.length);
        Arrays.stream(joins).map(Joins.Join::value).forEachOrdered(joiner::append);
        return joiner.toString();
    }

    private static String resolveJoin(Object query, List<Object> argList, String join) {
        Matcher matcher = PTN_PLACE_HOLDER.matcher(join);

        StringBuffer sb = new StringBuffer(SPACE);
        while (matcher.find()) {
            String fieldName = matcher.group(1);
            Field field = getField(query, fieldName);
            Object value = readField(field, query);
            argList.add(value);
            writeField(field, query, null);
            matcher.appendReplacement(sb, PLACE_HOLDER);
        }

        return matcher.appendTail(sb).toString();
    }

    @SuppressWarnings("java:S4973")
    private String build(PageQuery pageQuery, List<Object> argList, String... columns) {
        pageQuery = SerializationUtils.clone(pageQuery);

        String join = resolveJoin(pageQuery, argList, joinSql);
        String from = tableName + join;
        String sql = buildStart(columns, from);
        sql += buildWhere(pageQuery, argList);

        Joins joins = entityClass.getAnnotation(Joins.class);
        if (!joins.groupBy().isEmpty()) {
            sql += " GROUP BY " + joins.groupBy();
        }
        if (!joins.having().isEmpty()) {
            sql += " HAVING " + joins.having();
        }
        // intentionally use ==
        if (!(columns.length == 1 && COUNT == columns[0])) {
            // not SELECT COUNT(*)
            sql += buildOrderBy(pageQuery);
            sql = buildPaging(sql, pageQuery);
        }
        return sql;
    }

    public SqlAndArgs buildJoinSelectAndArgs(PageQuery query) {
        return SqlAndArgs.buildSqlWithArgs(argList -> build(query, argList, columnsForSelect));
    }

    public SqlAndArgs buildJoinCountAndArgs(PageQuery query) {
        return SqlAndArgs.buildSqlWithArgs((argList -> build(query, argList, COUNT)));
    }
}
