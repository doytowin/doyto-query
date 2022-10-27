/*
 * Copyright © 2019-2022 Forb Yuan
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

package win.doyto.query.mongodb.entity;

import lombok.experimental.UtilityClass;
import org.bson.types.ObjectId;
import win.doyto.query.util.BeanUtil;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * ObjectIdMapper
 *
 * @author f0rb on 2021-11-27
 */
@UtilityClass
public class ObjectIdMapper {
    private static final Map<Class<?>, Function<ObjectId, ?>> classFuncMap = new ConcurrentHashMap<>();

    static void initIdMapper(Class<? extends MongoPersistable> aClass) {
        classFuncMap.computeIfAbsent(aClass, clazz -> {
            Class<?> idType = BeanUtil.getIdClass(clazz);
            Function<ObjectId, ?> setIdFunc;
            if (idType.isAssignableFrom(String.class)) {
                setIdFunc = ObjectId::toHexString;
            } else if (idType.isAssignableFrom(ObjectId.class)) {
                setIdFunc = objectId -> objectId;
            } else if (idType.isAssignableFrom(BigInteger.class)) {
                setIdFunc = objectId -> new BigInteger(objectId.toHexString(), 16);
            } else {
                throw new UnsupportedIdTypeException(idType);
            }
            return setIdFunc;
        });
    }

    @SuppressWarnings("unchecked")
    public static <I> I convert(Class<?> clazz, ObjectId objectId) {
        return (I) classFuncMap.get(clazz).apply(objectId);
    }
}
