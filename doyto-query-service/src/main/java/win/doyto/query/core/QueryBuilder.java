package win.doyto.query.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.util.List;
import java.util.function.BiFunction;
import javax.persistence.Id;
import javax.persistence.Table;

import static win.doyto.query.core.CommonUtil.*;
import static win.doyto.query.core.Constant.*;

/**
 * QueryBuilder
 *
 * @author f0rb
 */
@Slf4j
public class QueryBuilder {

    protected static final String EQUALS_PLACE_HOLDER = " = " + Constant.PLACE_HOLDER;

    protected final String tableName;
    protected final String idColumn;
    protected final String whereId;
    private final BiFunction<IdWrapper<?>, String, String> resolveTableNameFunc;

    public QueryBuilder(String tableName, String idColumn) {
        this.tableName = tableName;
        this.idColumn = idColumn;
        this.whereId = WHERE + idColumn + EQUALS_PLACE_HOLDER;
        this.resolveTableNameFunc = isDynamicTable(tableName) ? CommonUtil::replaceHolderInString : (idWrapper, tableName1) -> tableName1;
    }

    public QueryBuilder(Class<?> entityClass) {
        this(resolveTableName(entityClass), resolveIdColumn(entityClass));
    }

    private static String resolveTableName(Class<?> entityClass) {
        return entityClass.getAnnotation(Table.class).name();
    }

    private static String resolveIdColumn(Class<?> entityClass) {
        return resolveColumn(FieldUtils.getFieldsWithAnnotation(entityClass, Id.class)[0]);
    }

    protected String resolveTableName(IdWrapper<?> idWrapper) {
        return resolveTableNameFunc.apply(idWrapper, tableName);
    }

    @SuppressWarnings("java:S4973")
    private String build(PageQuery pageQuery, List<Object> argList, String... columns) {
        String sql = BuildHelper.buildStart(columns, resolveTableName(pageQuery.toIdWrapper()));
        sql = replaceHolderInString(pageQuery, sql);
        sql += BuildHelper.buildWhere(pageQuery, argList);
        // intentionally use ==
        if (!(columns.length == 1 && COUNT == columns[0])) {
            // not SELECT COUNT(*)
            sql += BuildHelper.buildOrderBy(pageQuery);
            sql = BuildHelper.buildPaging(sql, pageQuery);
        }
        return sql;
    }

    String buildSelectAndArgs(PageQuery query, List<Object> argList) {
        return build(query, argList, "*");
    }

    public SqlAndArgs buildCountAndArgs(PageQuery query) {
        return SqlAndArgs.buildSqlWithArgs(argList -> build(query, argList, COUNT));
    }

    public SqlAndArgs buildSelectColumnsAndArgs(PageQuery query, String... columns) {
        return SqlAndArgs.buildSqlWithArgs(argList -> build(query, argList, columns));
    }

    public SqlAndArgs buildSelectById(IdWrapper<?> idWrapper, String... columns) {
        return SqlAndArgs.buildSqlWithArgs(argList -> {
            argList.add(idWrapper.getId());
            String columnStr = buildColumnStr(idWrapper, columns);
            String table = resolveTableName(idWrapper);
            return SELECT + columnStr + FROM + table + whereId;
        });
    }

    private String buildColumnStr(IdWrapper<?> idWrapper, String[] columns) {
        String columnStr;
        if (columns.length == 0) {
            columnStr = "*";
        } else {
            columnStr = replaceHolderInString(idWrapper, StringUtils.join(columns, SEPARATOR));
        }
        return columnStr;
    }

    protected SqlAndArgs buildSelectIdAndArgs(PageQuery query) {
        return buildSelectColumnsAndArgs(query, idColumn);
    }

}
