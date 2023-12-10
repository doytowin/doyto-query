/*
 * Copyright © 2019-2023 Forb Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.doyto.query.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import win.doyto.query.config.GlobalConfiguration;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * CommonUtil
 *
 * @author f0rb on 2019-05-31
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonUtil {

    public static final Collector<CharSequence, ?, String> CLT_COMMA_WITH_PAREN
            = Collectors.joining(", ", "(", ")");
    private static final Pattern PTN_REPLACE = Pattern.compile("\\w*");
    private static final Pattern PTN_DOLLAR_EX = Pattern.compile("\\$\\{(\\w+)}");
    private static final Pattern PTN_SPLIT_OR = Pattern.compile("Or(?=[A-Z])");

    public static boolean isDynamicTable(String input) {
        return PTN_DOLLAR_EX.matcher(input).find();
    }

    public static String replaceHolderInString(Object target, String input) {
        Matcher matcher = PTN_DOLLAR_EX.matcher(input);
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
            } else {
                log.warn("Unexpected argument: {}", replacement);
            }
        } while (matcher.find());
        return matcher.appendTail(sb).toString();
    }

    public static Object readFieldGetter(Object target, String fieldName) {
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
        Field field = getField(target, fieldName);
        if (field == null) {
            return null;
        }
        return readField(field, target);
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

    public static String escapeLike(String like) {
        return StringUtils.isBlank(like) ? like : "%" + escape(like) + "%";
    }

    public static String escapeStart(String like) {
        return StringUtils.isBlank(like) ? like : escape(like) + "%";
    }

    public static String escapeEnd(String like) {
        return StringUtils.isBlank(like) ? like : "%" + escape(like);
    }

    static String escape(String like) {
        return GlobalConfiguration.instance().getWildcardPtn().matcher(like).replaceAll("\\\\$0");
    }

    /**
     * @deprecated use {@link StringUtils#uncapitalize(String)} instead.
     */
    @Deprecated
    @SuppressWarnings("java:S1133")
    public static String camelize(String input) {
        char[] chars = input.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    public static String[] splitByOr(String columnName) {
        return Arrays.stream(PTN_SPLIT_OR.split(columnName, 0))
                     .map(StringUtils::uncapitalize).toArray(String[]::new);
    }

    public static boolean containsOr(String input) {
        return PTN_SPLIT_OR.matcher(input).find();
    }

    public static String toCamelCase(String input) {
        String[] parts = StringUtils.split(input, "_");
        StringBuilder result = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            result.append(StringUtils.capitalize(parts[i]));
        }
        return result.toString();
    }

    /**
     * Resolve the generic type of the field.
     *
     * @param field The type of the field should contains one and only one
     *              generic parameter, e.g., {@code List<UserView> users;}
     */
    @SuppressWarnings("unchecked")
    public static <R> Class<R> resolveActualReturnClass(Field field) {
        ParameterizedType genericType = (ParameterizedType) field.getGenericType();
        Type[] actualTypeArguments = genericType.getActualTypeArguments();
        return (Class<R>) actualTypeArguments[0];
    }
}
