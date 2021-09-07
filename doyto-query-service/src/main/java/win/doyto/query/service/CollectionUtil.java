package win.doyto.query.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * CollectionUtil
 *
 * @author f0rb
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CollectionUtil {
    public static <E> E first(List<E> iterable) {
        return first(iterable, null);
    }

    public static <E> E first(List<E> list, Object info) {
        try {
            return list.isEmpty() ? null : list.get(0);
        } finally {
            if (list.size() > 1) {
                debugRepetitiveElements(list, info);
            }
        }
    }

    static <E> void debugRepetitiveElements(List<E> list, Object info) {
        StringBuilder sb = new StringBuilder();
        int size = list.size();
        for (int i = 0, end = Math.min(3, size); i < end; i++) {
            E e = list.get(i);
            sb.append("\n").append(objectDetail(e));
        }
        if (size > 3) {
            sb.append("\n...");
        }
        log.warn("Found {} elements of {}{}", size, list.get(0).getClass(),
                 info == null ? "" : " for query: " + objectDetail(info));
        log.warn("Repetitive elements: {}", sb.toString());
        log.warn("\n  - {}", StringUtils.join(Arrays.copyOfRange(new Exception().getStackTrace(), 1, 6), "\n  - "));
    }

    private static String objectDetail(Object target) {
        return target instanceof String ? (String) target : ToStringBuilder.reflectionToString(target, NonNullToStringStyle.NON_NULL_STYLE);
    }
}
