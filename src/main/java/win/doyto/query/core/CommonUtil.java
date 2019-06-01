package win.doyto.query.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import win.doyto.query.config.GlobalConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * CommonUtil
 *
 * @author f0rb on 2019-05-31
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CommonUtil {

    private static final Pattern PTN_$EX = Pattern.compile("\\$\\{(\\w+)}");
    private static final Pattern PTN_CAPITAL_CHAR = Pattern.compile("([A-Z])");

    static boolean isDynamicTable(String input) {
        return PTN_$EX.matcher(input).find();
    }

    static String replaceTableName(Object entity, String tableName) {
        Matcher matcher = PTN_$EX.matcher(tableName);
        if (!matcher.find()) {
            return tableName;
        }

        StringBuffer sb = new StringBuffer();
        do {
            String fieldName = matcher.group(1);
            matcher = matcher.appendReplacement(sb, String.valueOf(readField(entity, fieldName)));
        } while (matcher.find());
        return sb.toString();
    }

    static Object readFieldGetter(Field field, Object target) {
        Object value;
        try {
            String fieldName = field.getName();
            String prefix = field.getType().isAssignableFrom(boolean.class) ? "is" : "get";
            value = MethodUtils.invokeMethod(target, true, prefix + StringUtils.capitalize(fieldName));
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            log.warn("is/get调用异常 : {}-{}", e.getClass().getName(), e.getMessage());
            value = readField(field, target);
        }
        if (value instanceof Enum) {
            Enumerated enumerated = field.getAnnotation(Enumerated.class);
            if (enumerated != null && enumerated.value() == EnumType.STRING) {
                value = value.toString();
            } else {
                value = ((Enum) value).ordinal();
            }
        }
        return value;
    }

    @SneakyThrows
    static Object readField(Field field, Object target) {
        return FieldUtils.readField(field, target, true);
    }

    static Object readField(Object target, String fieldName) {
        Field field = FieldUtils.getField(target.getClass(), fieldName, true);
        return readField(field, target);
    }

    static boolean ignoreField(Field field) {
        return field.getName().startsWith("$")              // $jacocoData
            || Modifier.isStatic(field.getModifiers())      // static field
            || field.isAnnotationPresent(Id.class)          // id
            || field.isAnnotationPresent(Transient.class)   // Transient field
            ;
    }

    static String wrapWithParenthesis(String input) {
        return "(" + input + ")";
    }

    static String escapeLike(String like) {
        if (StringUtils.isBlank(like)) {
            return like;
        }
        return "%" + like.replaceAll("[%|_]", "\\\\$0") + "%";
    }

    static String convertColumn(String columnName) {
        if (GlobalConfiguration.instance().isMapCamelCaseToUnderscore()) {
            columnName = camelCaseToUnderscore(columnName);
        }
        return columnName;
    }

    private static String camelCaseToUnderscore(String camel) {
        return PTN_CAPITAL_CHAR.matcher(camel).replaceAll("_$1").toLowerCase();
    }
}
