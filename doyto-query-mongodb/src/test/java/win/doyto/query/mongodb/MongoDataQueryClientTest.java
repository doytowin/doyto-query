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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import win.doyto.query.core.DataQueryClient;
import win.doyto.query.mongodb.test.aggregate.QuantityByStatusQuery;
import win.doyto.query.mongodb.test.aggregate.QuantityByStatusView;
import win.doyto.query.mongodb.test.aggregate.QuantityHaving;
import win.doyto.query.mongodb.test.aggregate.QuantityView;
import win.doyto.query.mongodb.test.inventory.InventoryQuery;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MongoDataQueryClientTest
 *
 * @author f0rb on 2022-01-25
 */
@ResourceLock(value = "inventory")
class MongoDataQueryClientTest extends MongoApplicationTest {

    DataQueryClient dataQueryClient;

    MongoDataQueryClientTest(@Autowired MongoClient mongoClient) {
        this.dataQueryClient = new MongoDataQueryClient(mongoClient);
    }

    @Test
    void aggregateQuery() {
        InventoryQuery inventoryQuery = InventoryQuery.builder().build();
        List<QuantityView> views = dataQueryClient.query(inventoryQuery, QuantityView.class);
        assertThat(views).hasSize(1)
                         .first()
                         .hasFieldOrPropertyWithValue("sumQty", 295)
                         .hasFieldOrPropertyWithValue("maxQty", 100)
                         .hasFieldOrPropertyWithValue("minQty", 25)
                         .hasFieldOrPropertyWithValue("avgQty", 59.0)
                         .hasFieldOrPropertyWithValue("firstQty", 25)
                         .hasFieldOrPropertyWithValue("lastQty", 45)
                         .hasFieldOrPropertyWithValue("stdDevPopQty", 25.96150997149434)
                         .hasFieldOrPropertyWithValue("stdDevSampQty", 29.025850547399987)
        ;
    }

    @Test
    void groupQuery() {
        QuantityByStatusQuery query = QuantityByStatusQuery.builder().build();
        List<QuantityByStatusView> views = dataQueryClient.query(query);
        assertThat(views).hasSize(2)
                         .first()
                         .extracting("sumQty", "status", "addToSetItem")
                         .containsExactly(120, "A", Arrays.asList("notebook", "postcard", "journal"))
        ;
        assertThat(views.get(0).getPushItemStatuses())
                .hasSize(3)
                .first()
                .extracting("item", "qty")
                .containsExactly("journal", 25)
        ;
    }

    @Test
    void groupQueryWithSort() {
        QuantityByStatusQuery query = QuantityByStatusQuery.builder().sort("status,desc").build();
        List<QuantityByStatusView> views = dataQueryClient.query(query);
        assertThat(views).hasSize(2)
                         .first()
                         .extracting("sumQty", "status")
                         .containsExactly(175, "D")
        ;
    }

    @Test
    void groupByHaving() {
        QuantityHaving having = QuantityHaving.builder().statusNot("D").build();
        QuantityByStatusQuery query = QuantityByStatusQuery.builder().having(having).build();
        List<QuantityByStatusView> views = dataQueryClient.query(query);
        assertThat(views).hasSize(1)
                         .first()
                         .extracting("sumQty", "status")
                         .containsExactly(120, "A")
        ;
    }

}