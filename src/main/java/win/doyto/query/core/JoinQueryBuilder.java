package win.doyto.query.core;

import org.apache.commons.lang3.SerializationUtils;
import win.doyto.query.annotation.Joins;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.Table;

import static win.doyto.query.core.CommonUtil.*;
import static win.doyto.query.core.Constant.REPLACE_HOLDER;
import static win.doyto.query.core.Constant.SPACE;

/**
 * JoinQueryBuilder
 *
 * @author f0rb on 2019-06-09
 */
class JoinQueryBuilder {

    private static final Pattern PTN_PLACE_HOLDER = Pattern.compile("#\\{(\\w+)}");

    protected final String tableName;
    protected String[] columnsForSelect;
    private Class<?> entityClass;

    public JoinQueryBuilder(Class<?> entityClass) {
        this.entityClass = entityClass;
        tableName = entityClass.getAnnotation(Table.class).name();
        columnsForSelect = Arrays
            .stream(entityClass.getDeclaredFields())
            .filter(field -> !ignoreField(field))
            .map(field -> QueryBuilder.resolveColumn(field) + " AS " + field.getName())
            .toArray(String[]::new);
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
            matcher = matcher.appendReplacement(sb, REPLACE_HOLDER);
        }

        return matcher.appendTail(sb).toString();
    }

    private String build(PageQuery pageQuery, List<Object> argList, String... columns) {
        Joins.Join[] joins = entityClass.getAnnotation(Joins.class).value();
        StringJoiner joiner = new StringJoiner(SPACE, joins.length);
        Arrays.stream(joins).map(Joins.Join::value).forEachOrdered(joiner::append);
        pageQuery = SerializationUtils.clone(pageQuery);
        String join = resolveJoin(pageQuery, argList, joiner.toString());
        return QueryBuilder.build(pageQuery, argList, Constant.SELECT, columns, tableName + join);
    }

    public SqlAndArgs buildJoinSelectAndArgs(PageQuery query) {
        ArrayList<Object> argList = new ArrayList<>();
        return new SqlAndArgs(build(query, argList, columnsForSelect), argList);
    }

}
