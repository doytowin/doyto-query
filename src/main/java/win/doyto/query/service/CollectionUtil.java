package win.doyto.query.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;
import java.util.Iterator;

/**
 * CollectionUtil
 *
 * @author f0rb
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CollectionUtil {
    public static <E> E first(Iterable<E> iterable) {
        Iterator<E> iterator = iterable.iterator();
        try {
            return iterator.hasNext() ? iterator.next() : null;
        } finally {
            if (iterator.hasNext()) {
                StringBuilder sb = new StringBuilder();
                int cnt = 0;
                for (E e : iterable) {
                    if (cnt < 3) {
                        sb.append("\n").append(e instanceof String ? e : ToStringBuilder.reflectionToString(e, NonNullToStringStyle.NON_NULL_STYLE));
                    }
                    cnt++;
                }
                if (cnt > 3) {
                    sb.append("\n...");
                }

                Class<?> clazz = iterator.next().getClass();
                log.warn(String.format("Find %d elements of %s", cnt, clazz));
                log.warn("Repetitive elements: {}", sb.toString());
                log.warn("\n  - {}", StringUtils.join(Arrays.copyOfRange(new Exception().getStackTrace(), 0, 5), "\n  - "));
            }
        }
    }
}
