package win.doyto.query.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mongodb.MongoClient;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import win.doyto.query.data.inventory.InventoryEntity;
import win.doyto.query.data.inventory.InventoryQuery;
import win.doyto.query.data.inventory.InventorySize;
import win.doyto.query.data.inventory.SizeQuery;
import win.doyto.query.util.BeanUtil;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MongoDataAccessTest
 *
 * @author f0rb on 2021-11-23
 */
@ActiveProfiles("test")
@DataMongoTest
@SpringBootApplication
class MongoDataAccessTest {
    MongoDataAccess<InventoryEntity, String, InventoryQuery> mongoDataAccess;

    public MongoDataAccessTest(@Autowired MongoClient mongoClient) {
        this.mongoDataAccess = new MongoDataAccess<>(mongoClient, InventoryEntity.class);
    }

    @BeforeEach
    void setUp() throws IOException {
        List<? extends Document> data = BeanUtil.loadJsonData("inventory/inventory.json", new TypeReference<List<? extends Document>>() {});
        mongoDataAccess.getCollection().insertMany(data);
    }

    @AfterEach
    void tearDown() {
        mongoDataAccess.getCollection().drop();
    }

    @Test
    void query() {
        InventoryQuery query = InventoryQuery.builder().build();
        List<InventoryEntity> list = mongoDataAccess.query(query);
        assertThat(list)
                .hasSize(5)
                .element(1)
                .extracting("item", "qty", "status", "size.h", "size.w", "size.uom")
                .containsExactly("notebook", 50, "A", 8.5, 11d, "in");
    }

    @Test
    void get() {
        InventoryQuery query = InventoryQuery.builder().build();
        List<InventoryEntity> list = mongoDataAccess.query(query);

        String id = list.get(0).getId();
        InventoryEntity inventoryEntity = mongoDataAccess.get(id);
        assertThat(inventoryEntity).isEqualToIgnoringGivenFields(list.get(0), "size");
    }

    @Test
    void queryByStatus() {
        InventoryQuery query = InventoryQuery.builder().status("A").build();
        List<InventoryEntity> list = mongoDataAccess.query(query);
        assertThat(list)
                .hasSize(3)
                .element(1)
                .extracting("item", "qty", "status", "size.h", "size.w", "size.uom")
                .containsExactly("notebook", 50, "A", 8.5, 11d, "in");
    }

    @Test
    void queryByStatusAndItemContain() {
        InventoryQuery query = InventoryQuery.builder().itemContain("t").status("A").build();
        List<InventoryEntity> list = mongoDataAccess.query(query);
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
        List<InventoryEntity> list = mongoDataAccess.query(query);
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
        long count = mongoDataAccess.count(query);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void deleteBySize$hLt10AndStatusEqA() {
        SizeQuery sizeQuery = SizeQuery.builder().hLt(10).build();
        InventoryQuery query = InventoryQuery.builder().size(sizeQuery).status("A").build();
        long deleted = mongoDataAccess.delete(query);
        assertThat(deleted).isEqualTo(1);

        long left = mongoDataAccess.count(new InventoryQuery());
        assertThat(left).isEqualTo(4);
    }

    @Test
    void update() {
        InventoryQuery query = InventoryQuery.builder().build();
        List<InventoryEntity> list = mongoDataAccess.query(query);

        InventoryEntity first = list.get(0);
        first.setStatus("C");
        first.getSize().setH(100.);
        int updateCount = mongoDataAccess.update(first);
        assertThat(updateCount).isEqualTo(1);

        //id remains the same and `status` changes to C and `size.h` changes to 100.
        InventoryEntity updated = mongoDataAccess.get(first.getId());
        assertThat(updated)
                .extracting("id", "status", "size.h")
                .containsExactly(first.getId(), "C", 100.)
        ;
    }

    @Test
    void create() {
        InventoryEntity origin = new InventoryEntity();
        origin.setItem("bookshelf");
        origin.setQty(20);
        origin.setStatus("D");
        InventorySize size = new InventorySize();
        size.setH(100.);
        size.setW(40.);
        size.setUom("in");
        origin.setSize(size);
        mongoDataAccess.create(origin);

        InventoryEntity fromDB = mongoDataAccess.get(origin.getId());
        assertThat(fromDB).isEqualToIgnoringGivenFields(fromDB, "size");

    }

    @Test
    void createSubDocument(@Autowired MongoClient mongoClient) {
        MongoDataAccess<InventorySize, ObjectId, SizeQuery> sizeDataAccess
                = new MongoDataAccess<>(mongoClient, InventorySize.class);
        InventoryEntity inventoryEntity = mongoDataAccess.query(InventoryQuery.builder().build()).get(0);

        InventorySize size = inventoryEntity.getSize();
        sizeDataAccess.create(size);
        assertThat(size.getId()).isInstanceOf(ObjectId.class);
        assertThat(size.getOid()).isInstanceOf(ObjectId.class);
    }

    @Test
    void paging() {
        // query [2,4)
        InventoryQuery testQuery = InventoryQuery.builder().pageNumber(1).pageSize(2).build();
        assertThat(mongoDataAccess.count(testQuery)).isEqualTo(5);
        assertThat(mongoDataAccess.query(testQuery))
                .extracting("item")
                .containsExactly("paper", "planner")
        ;
    }

    @Test
    void deleteById() {
        SizeQuery sizeQuery = SizeQuery.builder().hLt(10).build();
        InventoryQuery query = InventoryQuery.builder().size(sizeQuery).status("A").build();
        List<InventoryEntity> list = mongoDataAccess.query(query);

        //when
        int deleted = mongoDataAccess.delete(list.get(0).getId());
        assertThat(deleted).isEqualTo(1);

        long left = mongoDataAccess.count(new InventoryQuery());
        assertThat(left).isEqualTo(4);
    }

    @Test
    void patch() {
        InventoryQuery query = InventoryQuery.builder().build();
        InventoryEntity notebook = mongoDataAccess.query(query).get(1);
        InventoryEntity patch = new InventoryEntity();
        patch.setId(notebook.getId());
        patch.setStatus("P");
        InventorySize size = new InventorySize();
        size.setH(20.);
        patch.setSize(size);

        //when
        int count = mongoDataAccess.patch(patch);

        //then
        assertThat(count).isEqualTo(1);
        assertThat(mongoDataAccess.get(notebook.getId()))
                .extracting("item", "qty", "status", "size.h", "size.w", "size.uom")
                .containsExactly("notebook", 50, "P", 20., 11., "in");
    }

    @Test
    void patchByQuery() {
        InventoryQuery query = InventoryQuery.builder().status("A").build();
        InventoryEntity patch = new InventoryEntity();
        patch.setStatus("P");

        //when
        int count = mongoDataAccess.patch(patch, query);

        //then
        assertThat(count).isEqualTo(3);
    }

    @Test
    void queryColumns() {
        InventoryQuery query = InventoryQuery.builder().build();
        List<InventoryEntity> entities = mongoDataAccess.queryColumns(query, InventoryEntity.class, "item", "size.h");
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
        List<ObjectId> ids = mongoDataAccess.queryOid(query);
        List<InventoryEntity> entities = mongoDataAccess.query(query);

        //then
        assertThat(entities).extracting("oid").hasSameElementsAs(ids);
    }

    @Test
    void queryFirstLevelSingleColumn() {
        InventoryQuery query = InventoryQuery.builder().pageSize(2).build();
        List<String> items = mongoDataAccess.queryColumns(query, String.class, "item");
        assertThat(items).containsExactly("journal", "notebook");
    }

    @Test
    void queryNestedSingleColumn() {
        InventoryQuery query = InventoryQuery.builder().pageSize(2).build();
        List<String> items = mongoDataAccess.queryColumns(query, String.class, "size.uom");
        assertThat(items).containsExactly("cm", "in");
    }

    @Test
    void patchWithPage() {
        // only change [2, 4) inventories' status to F
        InventoryQuery query = InventoryQuery.builder().pageNumber(1).pageSize(2).build();
        InventoryEntity patch = new InventoryEntity();
        patch.setStatus("F");

        int count = mongoDataAccess.patch(patch, query);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void deleteWithPage() {
        // only change [2, 4) inventories' status to F
        InventoryQuery query = InventoryQuery.builder().pageNumber(1).pageSize(2).build();

        int count = mongoDataAccess.delete(query);
        assertThat(count).isEqualTo(2);
    }
}
