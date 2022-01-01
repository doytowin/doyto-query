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