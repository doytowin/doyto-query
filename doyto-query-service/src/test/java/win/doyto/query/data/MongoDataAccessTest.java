package win.doyto.query.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mongodb.MongoClient;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import win.doyto.query.data.inventory.InventoryEntity;
import win.doyto.query.data.inventory.InventoryQuery;
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
}
