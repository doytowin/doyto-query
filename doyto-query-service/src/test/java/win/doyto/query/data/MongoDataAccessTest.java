package win.doyto.query.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mongodb.MongoClient;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import win.doyto.query.data.inventory.InventoryEntity;
import win.doyto.query.data.inventory.InventoryQuery;
import win.doyto.query.util.BeanUtil;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MongoDataAccessTest
 *
 * @author f0rb on 2021-11-23
 */
@DataMongoTest
@SpringBootApplication
class MongoDataAccessTest {
    MongoDataAccess<InventoryEntity, ObjectId, InventoryQuery> mongoDataAccess;

    @BeforeEach
    void setUp(@Autowired MongoClient mongoClient) throws IOException {
        mongoDataAccess = new MongoDataAccess<>(mongoClient, InventoryEntity.class);
        mongoDataAccess.getCollection().insertMany(
                BeanUtil.loadJsonData("inventory/inventory.json", new TypeReference<List<? extends Document>>() {}));
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
}
