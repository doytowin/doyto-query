package win.doyto.query.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.Dialect;
import win.doyto.query.core.PageQuery;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import javax.persistence.Column;
import javax.persistence.Transient;

/**
 * ColumnUtil
 *
 * @author f0rb on 2021-11-21
 */
@UtilityClass
public class ColumnUtil {

    private static final Pattern PTN_CAPITAL_CHAR = Pattern.compile("([A-Z])");
    private static final Map<Class<?>, Field[]> classFieldsMap = new ConcurrentHashMap<>();

    static {
        classFieldsMap.put(PageQuery.class, new Field[]{});
    }

    public static Field[] initFields(Class<?> queryClass) {
        return initFields(queryClass, field -> {});
    }

    public static Field[] initFields(Class<?> queryClass, Consumer<Field> fieldConsumer) {
        classFieldsMap.computeIfAbsent(queryClass, c -> {
            Field[] fields = Arrays.stream(c.getDeclaredFields()).filter(CommonUtil::fieldFilter).toArray(Field[]::new);
            Arrays.stream(fields).forEach(fieldConsumer);
            return fields;
        });
        return classFieldsMap.get(queryClass);
    }

    public static String convertColumn(String columnName) {
        return GlobalConfiguration.instance().isMapCamelCaseToUnderscore() ?
                camelCaseToUnderscore(columnName) : columnName;
    }

    private static String camelCaseToUnderscore(String camel) {
        return PTN_CAPITAL_CHAR.matcher(camel).replaceAll("_$1").toLowerCase();
    }

    public static String resolveColumn(Field field) {
        Column column = field.getAnnotation(Column.class);
        String columnName = column != null && !column.name().isEmpty() ? column.name() : convertColumn(field.getName());
        return GlobalConfiguration.dialect().wrapLabel(columnName);
    }

    public static String selectAs(Field field) {
        String columnName = resolveColumn(field);
        Dialect dialect = GlobalConfiguration.dialect();
        String fieldName = dialect.wrapLabel(field.getName());
        return columnName.equalsIgnoreCase(fieldName) ? columnName : columnName + " AS " + fieldName;
    }

    public static String[] resolveSelectColumns(Class<?> entityClass) {
        return Arrays
                .stream(FieldUtils.getAllFields(entityClass))
                .filter(ColumnUtil::shouldRetain)
                .map(ColumnUtil::selectAs)
                .toArray(String[]::new);
    }

    private static boolean shouldRetain(Field field) {
        return !field.getName().startsWith("$")              // $jacocoData
            && !Modifier.isStatic(field.getModifiers())      // static field
            && !field.isAnnotationPresent(Transient.class)   // Transient field
            ;
    }

    public static boolean isSingleColumn(String... columns) {
        return columns.length == 1 && !columns[0].contains(",");
    }
}
