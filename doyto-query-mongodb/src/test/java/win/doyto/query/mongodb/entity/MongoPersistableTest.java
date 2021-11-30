package win.doyto.query.mongodb.entity;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import win.doyto.query.mongodb.test.inventory.InventoryEntity;
import win.doyto.query.util.BeanUtil;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MongoPersistableTest
 *
 * @author f0rb on 2021-11-27
 */
class MongoPersistableTest {

    @Test
    void serialize() {
        InventoryEntity target = new InventoryEntity();
        target.setItem("test");
        target.setObjectId(new ObjectId("61a1c650bfaa2f4b6480df54"));
        String json = BeanUtil.stringify(target);
        assertThat(json).isEqualTo("{\"id\":\"61a1c650bfaa2f4b6480df54\",\"item\":\"test\",\"_id\":{\"$oid\":\"61a1c650bfaa2f4b6480df54\"}}");
    }
}