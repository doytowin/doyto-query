package win.doyto.query.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * BeanUtil
 *
 * @author f0rb
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanUtil {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper()
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static <T> T loadJsonData(String path, TypeReference<T> typeReference) throws IOException {
        return loadJsonData(typeReference.getClass().getResourceAsStream(path), typeReference);
    }

    public static <T> T loadJsonData(InputStream resourceAsStream, TypeReference<T> typeReference) throws IOException {
        return objectMapper.readValue(resourceAsStream, typeReference);
    }

    @SneakyThrows
    public static <T> T parse(String json, TypeReference<T> typeReference) {
        return objectMapper.readValue(json, typeReference);
    }

    @SneakyThrows
    public static <T> T convertTo(Object source, Class<T> targetType) {
        return objectMapper.readValue(objectMapper.writeValueAsBytes(source), targetType);
    }

    @SneakyThrows
    public static <T> T copyTo(Object from, T to) {
        return objectMapper.updateValue(to, from);
    }

    public static Type[] getActualTypeArguments(Class<?> clazz) {
        Type genericSuperclass = clazz;
        do {
            genericSuperclass = ((Class<?>) genericSuperclass).getGenericSuperclass();
        } while (!(genericSuperclass instanceof ParameterizedType));
        return ((ParameterizedType) genericSuperclass).getActualTypeArguments();
    }
}
