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

import com.fasterxml.jackson.core.type.TypeReference;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import lombok.SneakyThrows;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.mongodb.test.inventory.InventoryEntity;
import win.doyto.query.util.BeanUtil;

import java.util.List;

/**
 * MongoApplicationTest
 *
 * @author f0rb on 2022-01-25
 */
@ActiveProfiles("test")
@DataMongoTest(properties = {"spring.mongodb.embedded.version=4.0.21"})
abstract class MongoApplicationTest {

    private MongoCollection<Document> collection;
    private MongoDataAccess<InventoryEntity, String, DoytoQuery> dataAccess;

    @BeforeEach
    void setUp(@Autowired MongoClient mongoClient) {
        dataAccess = new MongoDataAccess<>(mongoClient, InventoryEntity.class);
        collection = dataAccess.getCollection();

        loadData("test/inventory/inventory.json");
    }

    @SneakyThrows
    protected void loadData(String path) {
        List<InventoryEntity> data = BeanUtil.loadJsonData(path, new TypeReference<List<InventoryEntity>>() {});
        dataAccess.batchInsert(data);
    }

    @AfterEach
    void tearDown() {
        collection.drop();
    }

}
