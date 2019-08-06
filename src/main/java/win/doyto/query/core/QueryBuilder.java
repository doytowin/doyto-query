package win.doyto.query.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.entity.Persistable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
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
    private static final Pattern PTN_REPLACE = Pattern.compile("\\w+");
    private static final Pattern PTN_SORT = Pattern.compile(",(asc|desc)", Pattern.CASE_INSENSITIVE);

    private static final Map<Class, Field[]> classFieldsMap = new ConcurrentHashMap<>();
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

    private String build(PageQuery pageQuery, List<Object> argList, String... columns) {
        return build(pageQuery, argList, Constant.SELECT, columns, tableName);
    }

    @SuppressWarnings("squid:S4973")
    static String build(PageQuery pageQuery, List<Object> argList, String operation, String[] columns, String from) {
        String sql;
        sql = buildStart(operation, columns, from);
        sql = buildWhere(sql, pageQuery, argList);
        // intentionally use ==
        if (!(columns.length == 1 && COUNT == columns[0])) {
            // not SELECT COUNT(*)
            sql = buildOrderBy(sql, pageQuery, operation);
            sql = buildPaging(sql, pageQuery);
        }
        return sql;
    }

    static String buildStart(String operation, String[] columns, String from) {
        return operation + StringUtils.join(columns, SEPARATOR) + FROM + from;
    }

    @SuppressWarnings("squid:S4973")
    static String buildOrderBy(String sql, PageQuery pageQuery, String operation) {
        // intentionally use ==
        if (SELECT == operation && pageQuery.getSort() != null) {
            sql += " ORDER BY " + PTN_SORT.matcher(pageQuery.getSort()).replaceAll(" $1").replaceAll(";", SEPARATOR);
        }
        return sql;
    }

    static String buildPaging(String sql, PageQuery pageQuery) {
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
                if (sql.contains("${" + fieldName + "}") && value instanceof String && PTN_REPLACE.matcher((String) value).matches()) {
                    sql = sql.replaceAll("\\$\\{" + fieldName + "}", String.valueOf(value));
                } else {
                    String and = FieldProcessor.execute(argList, field, value);
                    if (and != null) {
                        whereList.add(and);
                    }
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

    protected SqlAndArgs buildSelectById(Object entity, String... columns) {
        if (columns.length == 0) {
            columns = new String[]{"*"};
        }
        String selectFrom = SELECT + StringUtils.join(columns, SEPARATOR) + FROM;
        if (entity instanceof Persistable) {
            return new SqlAndArgs(
                selectFrom + replaceHolderInString(entity, tableName) + whereId,
                Collections.singletonList(((Persistable) entity).getId()));
        }
        return new SqlAndArgs(selectFrom + tableName + whereId, Collections.singletonList(entity));
    }

    protected SqlAndArgs buildSelectIdAndArgs(PageQuery query) {
        return buildSelectColumnsAndArgs(query, idColumn);
    }

}
