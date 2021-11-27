package win.doyto.query.entity;

import lombok.experimental.UtilityClass;
import org.bson.types.ObjectId;
import win.doyto.query.util.BeanUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * MongoIdMapper
 *
 * @author f0rb on 2021-11-27
 */
@UtilityClass
public class ObjectIdMapper {
    private static final Map<Class<?>, Function<ObjectId, ?>> classFuncMap = new HashMap<>();

    static void initIdMapper(Class<? extends MongoPersistable> aClass) {
        classFuncMap.computeIfAbsent(aClass, clazz -> {
            Class<?> idType = (Class<?>) BeanUtil.getActualTypeArguments(clazz)[0];
            Function<ObjectId, ?> setIdFunc;
            if (idType.isAssignableFrom(String.class)) {
                setIdFunc = ObjectId::toHexString;
            } else if (idType.isAssignableFrom(ObjectId.class)) {
                setIdFunc = objectId -> objectId;
            } else {
                setIdFunc = objectId -> null;
            }
            return setIdFunc;
        });
    }

    @SuppressWarnings("unchecked")
    public static <I> I convert(Class<?> clazz, ObjectId objectId) {
        return (I) classFuncMap.get(clazz).apply(objectId);
    }
}
