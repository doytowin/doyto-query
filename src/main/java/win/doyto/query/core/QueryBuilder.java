package win.doyto.query.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.util.ArrayList;
import java.util.Collections;
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

    protected static final String EQUALS_REPLACE_HOLDER = " = " + Constant.REPLACE_HOLDER;

    protected final String tableName;
    protected final String idColumn;
    protected final String whereId;
    private final BiFunction<IdWrapper<?>, String, String> resolveTableNameFunc;

    public QueryBuilder(String tableName, String idColumn) {
        this.tableName = tableName;
        this.idColumn = idColumn;
        this.whereId = WHERE + idColumn + EQUALS_REPLACE_HOLDER;
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

    private String build(PageQuery pageQuery, List<Object> argList, String... columns) {
        return BuildHelper.build(pageQuery, argList, Constant.SELECT, columns, resolveTableName(pageQuery.toIdWrapper()));
    }

    public String buildSelectAndArgs(PageQuery query, List<Object> argList) {
        return buildSelectColumnsAndArgs(query, argList, "*");
    }

    public String buildCountAndArgs(PageQuery query, List<Object> argList) {
        return build(query, argList, COUNT);
    }

    public SqlAndArgs buildCountAndArgs(PageQuery query) {
        ArrayList<Object> argList = new ArrayList<>();
        return new SqlAndArgs(buildCountAndArgs(query, argList), argList);
    }

    public String buildSelectColumnsAndArgs(PageQuery query, List<Object> argList, String... columns) {
        return build(query, argList, columns);
    }

    public SqlAndArgs buildSelectColumnsAndArgs(PageQuery query, String... columns) {
        ArrayList<Object> argList = new ArrayList<>();
        return new SqlAndArgs(buildSelectColumnsAndArgs(query, argList, columns), argList);
    }

    protected SqlAndArgs buildSelectById(IdWrapper<?> idWrapper, String... columns) {
        if (columns.length == 0) {
            columns = new String[]{"*"};
        }
        String columnStr = replaceHolderInString(idWrapper, StringUtils.join(columns, SEPARATOR));
        String selectFrom = SELECT + columnStr + FROM;
        String sql = selectFrom + resolveTableName(idWrapper) + whereId;
        return new SqlAndArgs(sql, Collections.singletonList(idWrapper.getId()));
    }

    protected SqlAndArgs buildSelectIdAndArgs(PageQuery query) {
        return buildSelectColumnsAndArgs(query, idColumn);
    }

}
