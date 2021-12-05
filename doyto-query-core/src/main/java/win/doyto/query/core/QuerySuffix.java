package win.doyto.query.core;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import win.doyto.query.geo.Box;
import win.doyto.query.geo.Circle;
import win.doyto.query.geo.Near;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * QuerySuffix
 *
 * @author f0rb on 2021-12-01
 */
@SuppressWarnings("java:S115")
@Slf4j
@AllArgsConstructor
public enum QuerySuffix {
    Not,
    NotLike(Constants.LIKE_PREDICATE),
    Like(Constants.LIKE_PREDICATE),
    Contain(Constants.LIKE_PREDICATE),
    Start(Constants.LIKE_PREDICATE),
    NotIn(new NotInPredicate()),
    In(new InPredicate()),
    NotNull,
    Null,
    Gt,
    Ge,
    Lt,
    Le,
    Eq,
    Near(Near.class::isInstance),
    NearSphere(Near.class::isInstance),
    Center(Circle.class::isInstance),
    CenterSphere(Circle.class::isInstance),
    Box(Box.class::isInstance),
    Py(Collection.class::isInstance), // short for Polygon

    NONE;

    private static final Pattern SUFFIX_PTN;

    static {
        List<String> suffixList = Arrays.stream(values()).filter(querySuffix -> querySuffix != NONE).map(Enum::name).collect(Collectors.toList());
        String suffixPtn = StringUtils.join(suffixList, "|");
        SUFFIX_PTN = Pattern.compile("(" + suffixPtn + ")$");
    }

    QuerySuffix() {
        this(c -> true);
    }

    private Predicate<Object> typeValidator;

    public static QuerySuffix resolve(String fieldName) {
        Matcher matcher = SUFFIX_PTN.matcher(fieldName);
        return matcher.find() ? valueOf(matcher.group()) : NONE;
    }

    public static boolean isValidValue(Object value, Field field) {
        return !(value == null
                || (value instanceof Boolean && field.getType().isPrimitive() && Boolean.FALSE.equals(value))
                || (resolve(field.getName()).shouldIgnore(value))
        );
    }

    public String resolveColumnName(String fieldName) {
        String suffix = this.name();
        return fieldName.endsWith(suffix) ? fieldName.substring(0, fieldName.length() - suffix.length()) : fieldName;
    }

    public boolean shouldIgnore(Object value) {
        return !typeValidator.test(value);
    }

    static class InPredicate implements Predicate<Object> {
        @Override
        public boolean test(Object o) {
            if (o instanceof Collection) {
                return true;
            }
            log.warn("Type of field which ends with In/NotIn should be Collection.");
            return false;
        }
    }

    static class NotInPredicate extends InPredicate {
        @Override
        public boolean test(Object o) {
            return super.test(o) && !((Collection<?>) o).isEmpty();
        }
    }

    @SuppressWarnings("java:S1214")
    private interface Constants {
        Predicate<Object> LIKE_PREDICATE = value -> {
            if (value instanceof String) {
                return !StringUtils.isBlank((String) value);
            }
            log.warn("Type of field which ends with Like should be String.");
            return false;
        };
    }
}
