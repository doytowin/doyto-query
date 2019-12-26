package win.doyto.query.demo.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;

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

    public static <T> T loadJsonData(String path, TypeReference<T> typeReference) throws IOException {
       return loadJsonData(typeReference.getClass().getResourceAsStream(path), typeReference);
    }
    public static <T> T loadJsonData(InputStream resourceAsStream, TypeReference<T> typeReference) throws IOException {
       return objectMapper.readValue(resourceAsStream, typeReference);
    }
}
