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
    void setNullForEntity() {
        ObjectIdMapper.initIdMapper(ShortEntity.class);

        ObjectId objectId = new ObjectId();
        Object id = ObjectIdMapper.convert(ShortEntity.class, objectId);

        assertNull(id);
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