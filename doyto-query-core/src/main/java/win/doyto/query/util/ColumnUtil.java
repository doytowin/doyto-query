package win.doyto.query.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.reflect.FieldUtils;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.Dialect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
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
