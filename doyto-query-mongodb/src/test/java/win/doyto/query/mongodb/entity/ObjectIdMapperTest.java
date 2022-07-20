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

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ObjectIdMapperTest
 *
 * @author f0rb on 2021-12-08
 */
class ObjectIdMapperTest {

    static class ObjectIdEntity extends MongoPersistable<ObjectId> {
    }

    @Test
    void setObjectIdForEntity() {
        ObjectIdMapper.initIdMapper(ObjectIdEntity.class);

        ObjectId objectId = new ObjectId();
        Object id = ObjectIdMapper.convert(ObjectIdEntity.class, objectId);

        assertSame(objectId, id);
    }

    static class ShortEntity extends MongoPersistable<Short> {
    }

    @Test
    void shouldThrowForUnsupportedIdType() {
        assertThrowsExactly(UnsupportedIdTypeException.class, () ->
                ObjectIdMapper.initIdMapper(ShortEntity.class));
    }

    static class BigIntegerEntity extends MongoPersistable<BigInteger> {
    }

    @Test
    void setBigIntegerForEntity() {
        ObjectIdMapper.initIdMapper(BigIntegerEntity.class);

        ObjectId objectId = new ObjectId();
        Object id = ObjectIdMapper.convert(BigIntegerEntity.class, objectId);

        assertEquals(id, new BigInteger(objectId.toHexString(), 16));
    }
}