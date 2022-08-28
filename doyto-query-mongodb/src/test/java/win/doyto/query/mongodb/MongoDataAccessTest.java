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

package win.doyto.query.mongodb;

import com.mongodb.client.MongoClient;
import org.assertj.core.groups.Tuple;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import win.doyto.query.mongodb.test.inventory.InventoryEntity;
import win.doyto.query.mongodb.test.inventory.InventoryQuery;
import win.doyto.query.mongodb.test.inventory.InventorySize;
import win.doyto.query.mongodb.test.inventory.SizeQuery;
import win.doyto.query.mongodb.test.role.RoleViewQuery;
import win.doyto.query.mongodb.test.user.UserView;
import win.doyto.query.mongodb.test.user.UserViewQuery;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ResourceAccessMode.READ_WRITE;

/**
 * MongoDataAccessTest
 *
 * @author f0rb on 2021-11-23
 */
@ResourceLock(value = "inventory", mode = READ_WRITE)
class MongoDataAccessTest extends MongoApplicationTest {
    MongoDataAccess<InventoryEntity, String, InventoryQuery> inventoryDataAccess;
    MongoDataAccess<UserView, BigInteger, UserViewQuery> userDataAccess;

    public MongoDataAccessTest(@Autowired MongoClient mongoClient) {
        this.inventoryDataAccess = new MongoDataAccess<>(mongoClient, InventoryEntity.class);
        this.userDataAccess = new MongoDataAccess<>(mongoClient, UserView.class);
    }

    @Test
    void query() {
        InventoryQuery query = InventoryQuery.builder().build();
        List<InventoryEntity> list = inventoryDataAccess.query(query);
        assertThat(list)
                  .hasSize(5)
                  .element(1)
                  .extracting("item", "qty", "status", "size.h", "size.w", "size.uom")
                  .containsExactly("notebook", 50, "A", 8.5, 11d, "in");
    }

    @Test
    void get() {
        InventoryQuery query = InventoryQuery.builder().build();
        List<InventoryEntity> list = inventoryDataAccess.query(query);

        String id = list.get(0).getId();
        InventoryEntity inventoryEntity = inventoryDataAccess.get(id);
        assertThat(inventoryEntity)
                .usingRecursiveComparison()
                .isEqualTo(list.get(0));
    }

    @Test
    void queryByStatus() {
        InventoryQuery query = InventoryQuery.builder().status("A").build();
        List<InventoryEntity> list = inventoryDataAccess.query(query);
        assertThat(list)
                  .hasSize(3)
                  .element(1)
                  .extracting("item", "qty", "status", "size.h", "size.w", "size.uom")
                  .containsExactly("notebook", 50, "A", 8.5, 11d, "in");
    }

    @Test
    void queryByStatusAndItemContain() {
        InventoryQuery query = InventoryQuery.builder().itemContain("t").status("A").build();
        List<InventoryEntity> list = inventoryDataAccess.query(query);
        assertThat(list)
                  .hasSize(2)
                  .first()
                  .extracting("item", "status")
                  .containsExactly("notebook", "A");
    }

    @Test
    void queryBySizeGt() {
        SizeQuery sizeQuery = SizeQuery.builder().hLt(10).build();
        InventoryQuery query = InventoryQuery.builder().size(sizeQuery).status("A").build();
        List<InventoryEntity> list = inventoryDataAccess.query(query);
        assertThat(list)
                  .hasSize(1)
                  .first()
                  .extracting("item", "status", "size.h")
                  .containsExactly("notebook", "A", 8.5);
    }

    @Test
    void countBySize$hLt10AndStatusEqA() {
        SizeQuery sizeQuery = SizeQuery.builder().hLt(10).build();
        InventoryQuery query = InventoryQuery.builder().size(sizeQuery).status("A").build();
        long count = inventoryDataAccess.count(query);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void deleteBySize$hLt10AndStatusEqA() {
        SizeQuery sizeQuery = SizeQuery.builder().hLt(10).build();
        InventoryQuery query = InventoryQuery.builder().size(sizeQuery).status("A").build();
        long deleted = inventoryDataAccess.delete(query);
        assertThat(deleted).isEqualTo(1);

        long left = inventoryDataAccess.count(new InventoryQuery());
        assertThat(left).isEqualTo(4);
    }

    @Test
    void update() {
        InventoryQuery query = InventoryQuery.builder().build();
        List<InventoryEntity> list = inventoryDataAccess.query(query);

        InventoryEntity first = list.get(0);
        first.setStatus("C");
        first.getSize().setH(100.);
        int updateCount = inventoryDataAccess.update(first);
        assertThat(updateCount).isEqualTo(1);

        //id remains the same and `status` changes to C and `size.h` changes to 100.
        InventoryEntity updated = inventoryDataAccess.get(first.getId());
        assertThat(updated)
                  .extracting("id", "status", "size.h")
                  .containsExactly(first.getId(), "C", 100.)
        ;
    }

    @Test
    void builder() {
        InventoryEntity origin = new InventoryEntity();
        origin.setItem("bookshelf");
        origin.setQty(20);
        origin.setStatus("D");
        InventorySize size = new InventorySize();
        size.setH(100.);
        size.setW(40.);
        size.setUom("in");
        origin.setSize(size);
        inventoryDataAccess.create(origin);

        InventoryEntity fromDB = inventoryDataAccess.get(origin.getId());
        assertThat(fromDB)
                .usingRecursiveComparison()
                .isEqualTo(origin);
    }

    @Test
    void createSubDocument(@Autowired MongoClient mongoClient) {
        MongoDataAccess<InventorySize, ObjectId, SizeQuery> sizeDataAccess
                = new MongoDataAccess<>(mongoClient, InventorySize.class);
        InventoryEntity inventoryEntity = inventoryDataAccess.query(InventoryQuery.builder().build()).get(0);

        InventorySize size = inventoryEntity.getSize();
        sizeDataAccess.create(size);
        assertThat(size.getId()).isInstanceOf(ObjectId.class);
        assertThat(size.getObjectId()).isInstanceOf(ObjectId.class);
    }

    @Test
    void paging() {
        // query [2,4)
        InventoryQuery testQuery = InventoryQuery.builder().pageNumber(2).pageSize(2).build();
        assertThat(inventoryDataAccess.count(testQuery)).isEqualTo(5);
        assertThat(inventoryDataAccess.query(testQuery))
                .extracting("item")
                .containsExactly("paper", "planner")
        ;
    }

    @Test
    void deleteById() {
        SizeQuery sizeQuery = SizeQuery.builder().hLt(10).build();
        InventoryQuery query = InventoryQuery.builder().size(sizeQuery).status("A").build();
        List<InventoryEntity> list = inventoryDataAccess.query(query);

        //when
        int deleted = inventoryDataAccess.delete(list.get(0).getId());
        assertThat(deleted).isEqualTo(1);

        long left = inventoryDataAccess.count(new InventoryQuery());
        assertThat(left).isEqualTo(4);
    }

    @Test
    void patch() {
        InventoryQuery query = InventoryQuery.builder().build();
        InventoryEntity notebook = inventoryDataAccess.query(query).get(1);
        InventoryEntity patch = new InventoryEntity();
        patch.setId(notebook.getId());
        patch.setStatus("P");
        InventorySize size = new InventorySize();
        size.setH(20.);
        patch.setSize(size);

        //when
        int count = inventoryDataAccess.patch(patch);

        //then
        assertThat(count).isEqualTo(1);
        assertThat(inventoryDataAccess.get(notebook.getId()))
                .extracting("item", "qty", "status", "size.h", "size.w", "size.uom")
                .containsExactly("notebook", 50, "P", 20., 11., "in");
    }

    @Test
    void patchByQuery() {
        InventoryQuery query = InventoryQuery.builder().status("A").build();
        InventoryEntity patch = new InventoryEntity();
        patch.setStatus("P");

        //when
        int count = inventoryDataAccess.patch(patch, query);

        //then
        assertThat(count).isEqualTo(3);
    }

    @Test
    void queryColumns() {
        InventoryQuery query = InventoryQuery.builder().build();
        List<InventoryEntity> entities = inventoryDataAccess.queryColumns(query, InventoryEntity.class, "item", "size.h");
        assertThat(entities)
                  .hasSize(5)
                  .first()
                  .extracting("item", "qty", "status", "size.h", "size.w", "size.uom")
                  .containsExactly("journal", null, null, 14.0, null, null);
    }

    @Test
    void queryOid() {
        InventoryQuery query = InventoryQuery.builder().status("A").build();

        //when
        List<ObjectId> ids = inventoryDataAccess.queryObjectId(query);
        List<InventoryEntity> entities = inventoryDataAccess.query(query);

        //then
        assertThat(entities).extracting("objectId").hasSameElementsAs(ids);
    }

    @Test
    void queryFirstLevelSingleColumn() {
        InventoryQuery query = InventoryQuery.builder().pageSize(2).build();
        List<String> items = inventoryDataAccess.queryColumns(query, String.class, "item");
        assertThat(items).containsExactly("journal", "notebook");
    }

    @Test
    void queryNestedSingleColumn() {
        InventoryQuery query = InventoryQuery.builder().pageSize(2).build();
        List<String> items = inventoryDataAccess.queryColumns(query, String.class, "size.uom");
        assertThat(items).containsExactly("cm", "in");
    }

    @Test
    void patchWithPage() {
        // only change [2, 4) inventories' status to F
        InventoryQuery query = InventoryQuery.builder().pageNumber(1).pageSize(2).build();
        InventoryEntity patch = new InventoryEntity();
        patch.setStatus("F");

        int count = inventoryDataAccess.patch(patch, query);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void deleteWithPage() {
        // only change [2, 4) inventories' status to F
        InventoryQuery query = InventoryQuery.builder().pageNumber(1).pageSize(2).build();

        int count = inventoryDataAccess.delete(query);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void queryNestedSingleColumnWithTypeDouble() {
        InventoryQuery query = InventoryQuery.builder().pageSize(2).build();
        List<Double> items = inventoryDataAccess.queryColumns(query, Double.class, "size.h");
        assertThat(items).containsExactly(14., 8.5);
    }

    @Test
    void sort() {
        InventoryQuery query = InventoryQuery.builder().sort("item,desc").build();
        List<InventoryEntity> inventoryEntities = inventoryDataAccess.query(query);
        assertThat(inventoryEntities)
                  .extracting("item")
                  .containsExactly("postcard", "planner", "paper", "notebook", "journal");
    }

    @Test
    void sortForNestedField() {
        InventoryQuery query = InventoryQuery.builder().sort("size.h;item,desc").build();
        List<InventoryEntity> inventoryEntities = inventoryDataAccess.query(query);
        assertThat(inventoryEntities)
                  .extracting("item", "size.h")
                  .containsExactly(
                        Tuple.tuple("paper", 8.5),
                        Tuple.tuple("notebook", 8.5),
                        Tuple.tuple("postcard", 10.0),
                        Tuple.tuple("journal", 14.0),
                        Tuple.tuple("planner", 22.85)
                );
    }

    @Test
    void queryIds() {
        InventoryQuery query = InventoryQuery.builder().status("A").build();

        //when
        List<String> ids = inventoryDataAccess.queryIds(query);
        List<InventoryEntity> entities = inventoryDataAccess.query(query);

        //then
        assertThat(entities).extracting("id").hasSameElementsAs(ids);
    }

    @Test
    void supportNestedQuery() {
        // Query users who are assigned valid role.
        RoleViewQuery roleViewQuery = RoleViewQuery.builder().valid(true).build();
        UserViewQuery userViewQuery = UserViewQuery.builder().role(roleViewQuery).build();
        List<UserView> userEntities = userDataAccess.query(userViewQuery);
        assertThat(userEntities).hasSize(2);
        assertThat(userEntities).extracting("username")
                                .containsExactly("f0rb", "user3");
    }
}
