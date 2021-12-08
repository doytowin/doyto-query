package win.doyto.query.mongodb.entity;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

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
}