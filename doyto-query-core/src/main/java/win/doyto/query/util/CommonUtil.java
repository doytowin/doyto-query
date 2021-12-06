package win.doyto.query.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Transient;

/**
 * CommonUtil
 *
 * @author f0rb on 2019-05-31
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonUtil {

    private static final Pattern PTN_REPLACE = Pattern.compile("\\w*");
    private static final Pattern PTN_$EX = Pattern.compile("\\$\\{(\\w+)}");
    private static final Pattern PTN_SPLIT_OR = Pattern.compile("Or(?=[A-Z])");

    public static boolean isDynamicTable(String input) {
        return PTN_$EX.matcher(input).find();
    }

    public static String replaceHolderInString(Object target, String input) {
        Matcher matcher = PTN_$EX.matcher(input);
        if (!matcher.find()) {
            return input;
        }

        StringBuffer sb = new StringBuffer();
        do {
            String fieldName = matcher.group(1);
            Object value = readFieldGetter(target, fieldName);
            String replacement = String.valueOf(value);
            if (PTN_REPLACE.matcher(replacement).matches()) {
                matcher.appendReplacement(sb, replacement);
            }
        } while (matcher.find());
        return matcher.appendTail(sb).toString();
    }

    static Object readFieldGetter(Object target, String fieldName) {
        Object value;
        try {
            value = MethodUtils.invokeMethod(target, true, "get" + StringUtils.capitalize(fieldName));
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            log.warn("is/get调用异常 : {}-{}", e.getClass().getName(), e.getMessage());
            value = readField(target, fieldName);
        }
        return value;
    }

    public static Object readFieldGetter(Field field, Object target) {
        Object value;
        try {
            String fieldName = field.getName();
            String prefix = field.getType().isAssignableFrom(boolean.class) ? "is" : "get";
            value = MethodUtils.invokeMethod(target, true, prefix + StringUtils.capitalize(fieldName));
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            log.warn("is/get调用异常 : {}-{}", e.getClass().getName(), e.getMessage());
            value = readField(field, target);
        }
        if (value instanceof Enum<?>) {
            Enumerated enumerated = field.getAnnotation(Enumerated.class);
            if (enumerated != null && enumerated.value() == EnumType.STRING) {
                value = value.toString();
            } else {
                value = ((Enum<?>) value).ordinal();
            }
        }
        return value;
    }

    @SneakyThrows
    public static Object readField(Field field, Object target) {
        return FieldUtils.readField(field, target, true);
    }

    public static Object readField(Object target, String fieldName) {
        return readField(getField(target, fieldName), target);
    }

    public static Field getField(Object target, String fieldName) {
        Field field = FieldUtils.getField(target.getClass(), fieldName, true);
        if (field == null) {
            log.warn("Field [{}] not found", fieldName);
        }
        return field;
    }

    @SneakyThrows
    public static void writeField(Field field, Object target, Object value) {
        FieldUtils.writeField(field, target, value, true);
    }

    public static boolean fieldFilter(Field field) {
        return !(field.getName().startsWith("$")              // $jacocoData
            || Modifier.isStatic(field.getModifiers())      // static field
            || field.isAnnotationPresent(GeneratedValue.class)// id
            || field.isAnnotationPresent(Transient.class)   // Transient field
        );
    }

    public static String wrapWithParenthesis(String input) {
        return "(" + input + ")";
    }

    public static String escapeLike(String like) {
        return StringUtils.isBlank(like) ? like : "%" + escape(like) + "%";
    }

    public static String escapeStart(String like) {
        return StringUtils.isBlank(like) ? like : escape(like) + "%";
    }

    private static String escape(String like) {
        return like.replaceAll("[%|_]", "\\\\$0");
    }

    static String camelize(String or) {
        return or.substring(0, 1).toLowerCase() + or.substring(1);
    }

    public static String[] splitByOr(String columnName) {
        return Arrays.stream(PTN_SPLIT_OR.split(columnName, 0))
                     .map(CommonUtil::camelize).toArray(String[]::new);
    }

    public static boolean containsOr(String input) {
        return PTN_SPLIT_OR.matcher(input).find();
    }

    public static String toCamelCase(String input) {
        String[] parts = input.split("_");
        StringBuilder result = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            result.append(StringUtils.capitalize(parts[i]));
        }
        return result.toString();
    }

}
