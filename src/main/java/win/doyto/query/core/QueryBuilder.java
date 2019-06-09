package win.doyto.query.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.config.GlobalConfiguration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.Column;
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
    private static final Map<Class, Field[]> classFieldsMap = new ConcurrentHashMap<>();
    private static final Pattern PTN_PLACE_HOLDER = Pattern.compile("#\\{(\\w+)}");
    protected static final String EQUALS_REPLACE_HOLDER = " = " + Constant.REPLACE_HOLDER;

    protected final String tableName;
    protected final String idColumn;
    protected final String whereId;

    public QueryBuilder(String tableName, String idColumn) {
        this.tableName = tableName;
        this.idColumn = idColumn;
        this.whereId = " WHERE " + idColumn + EQUALS_REPLACE_HOLDER;
    }

    public QueryBuilder(Class<?> entityClass) {
        tableName = entityClass.getAnnotation(Table.class).name();
        Field idField = FieldUtils.getFieldsWithAnnotation(entityClass, Id.class)[0];
        idColumn = resolveColumn(idField);
        whereId = " WHERE " + idColumn + EQUALS_REPLACE_HOLDER;
    }

    static String resolveColumn(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null && !column.name().isEmpty()) {
            return column.name();
        } else {
            return convertColumn(field.getName());
        }
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

    private String build(PageQuery pageQuery, List<Object> argList, String operation, String... columns) {
        return build(tableName, pageQuery, argList, operation, columns);
    }

    @SuppressWarnings("squid:S4973")
    protected static String build(String table, PageQuery pageQuery, List<Object> argList, String operation, String... columns) {

        String join = "";
        if (pageQuery.getJoin() != null) {
            pageQuery = SerializationUtils.clone(pageQuery);
            join = resolveJoin(pageQuery, argList, pageQuery.getJoin());
        }

        String sql;
        sql = buildStart(operation, columns, table);
        if (join != null) {
            sql += join;
        }
        sql = buildWhere(sql, pageQuery, argList);
        // intentionally use ==
        if (!(columns.length == 1 && COUNT == columns[0])) {
            // not SELECT COUNT(*)
            sql = buildOrderBy(sql, pageQuery, operation);
            sql = buildPaging(sql, pageQuery);
        }
        return sql;
    }

    private static String buildStart(String operation, String[] columns, String table) {
        return operation + StringUtils.join(columns, SEPARATOR) + FROM + table;
    }

    @SuppressWarnings("squid:S4973")
    private static String buildOrderBy(String sql, PageQuery pageQuery, String operation) {
        // intentionally use ==
        if (SELECT == operation && pageQuery.getSort() != null) {
            sql += " ORDER BY " + pageQuery.getSort().replaceAll(",", SPACE).replaceAll(";", ", ");
        }
        return sql;
    }

    private static String buildPaging(String sql, PageQuery pageQuery) {
        if (pageQuery.needPaging()) {
            sql = GlobalConfiguration.instance().getDialect().buildPageSql(sql, pageQuery.getPageSize(), pageQuery.getOffset());
        }
        return sql;
    }

    public static String buildWhere(String sql, Object query, List<Object> argList) {
        initFields(query);
        Field[] fields = classFieldsMap.get(query.getClass());
        List<Object> whereList = new ArrayList<>(fields.length);
        for (Field field : fields) {
            String fieldName = field.getName();
            Object value = readFieldGetter(field, query);
            if (isValidValue(value, field)) {
                if (sql.contains("${" + fieldName + "}") && StringUtils.isAlphanumeric(String.valueOf(value))) {
                    sql = sql.replaceAll("\\$\\{" + fieldName + "}", String.valueOf(value));
                } else {
                    whereList.add(FieldProcessor.execute(argList, field, value));
                }
            }
        }
        if (!whereList.isEmpty()) {
            sql += WHERE + StringUtils.join(whereList, " AND ");
        }
        return sql;
    }

    private static void initFields(Object query) {
        Class<?> clazz = query.getClass();
        if (!classFieldsMap.containsKey(clazz)) {
            classFieldsMap.put(clazz, Arrays.stream(clazz.getDeclaredFields()).filter(field -> !ignoreField(field)).toArray(Field[]::new));
            for (Field field : classFieldsMap.get(clazz)) {
                FieldProcessor.init(field);
            }
        }
    }

    public String buildSelectAndArgs(PageQuery query, List<Object> argList) {
        return buildSelectColumnsAndArgs(query, argList, "*");
    }

    public String buildCountAndArgs(PageQuery query, List<Object> argList) {
        return build(query, argList, SELECT, COUNT);
    }

    public SqlAndArgs buildCountAndArgs(PageQuery query) {
        ArrayList<Object> argList = new ArrayList<>();
        return new SqlAndArgs(buildCountAndArgs(query, argList), argList);
    }

    public String buildSelectColumnsAndArgs(PageQuery query, List<Object> argList, String... columns) {
        return build(query, argList, SELECT, columns);
    }

    public SqlAndArgs buildSelectColumnsAndArgs(PageQuery query, String... columns) {
        ArrayList<Object> argList = new ArrayList<>();
        return new SqlAndArgs(buildSelectColumnsAndArgs(query, argList, columns), argList);
    }

    public String buildSelectColumnsAndArgsAndJoin(PageQuery query, List<Object> argList, String join, String... columns) {
        return build(query.join(join), argList, SELECT, columns);
    }

    public SqlAndArgs buildSelectColumnsAndArgsAndJoin(PageQuery query, String join, String... columns) {
        ArrayList<Object> argList = new ArrayList<>();
        String sql = buildSelectColumnsAndArgsAndJoin(query, argList, join, columns);
        return new SqlAndArgs(sql, argList);
    }

    public String buildSelectById() {
        return "SELECT * FROM " + tableName + whereId;
    }

    protected String buildSelectById(Object entity) {
        return "SELECT * FROM " + replaceTableName(entity, tableName) + whereId;
    }

    protected SqlAndArgs buildSelectIdAndArgs(PageQuery query) {
        return buildSelectColumnsAndArgs(query, idColumn);
    }

}
