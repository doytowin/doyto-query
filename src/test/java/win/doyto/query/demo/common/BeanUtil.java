package win.doyto.query.demo.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

/**
 * BeanUtil
 *
 * @author f0rb
 */
public class BeanUtil {
    private BeanUtil() {
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public static <T> T copyFields(Object source, Class<T> target) {
       return objectMapper.readValue(objectMapper.writeValueAsString(source), target);
    }
}
