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
import win.doyto.query.mongodb.test.aggregate.QuantityByStatusView;
import win.doyto.query.mongodb.test.aggregate.QuantityView;
import win.doyto.query.mongodb.test.inventory.InventoryQuery;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ResourceAccessMode.READ_WRITE;

/**
 * MongoDataQueryTest
 *
 * @author f0rb on 2022-01-25
 */
@ResourceLock(value = "inventory", mode = READ_WRITE)
class MongoDataQueryTest extends MongoApplicationTest {

    MongoDataQuery mongoDataQuery;

    MongoDataQueryTest(@Autowired MongoClient mongoClient) {
        this.mongoDataQuery = new MongoDataQuery(mongoClient);
    }

    @Test
    void aggregateQuery() {
        List<QuantityView> views = mongoDataQuery.query(new InventoryQuery(), QuantityView.class);
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
        InventoryQuery query = new InventoryQuery();
        List<QuantityByStatusView> views = mongoDataQuery.query(query, QuantityByStatusView.class);
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
        InventoryQuery query = InventoryQuery.builder().sort("status,desc").build();
        List<QuantityByStatusView> views = mongoDataQuery.query(query, QuantityByStatusView.class);
        assertThat(views).hasSize(2)
                         .first()
                         .extracting("sumQty", "status")
                         .containsExactly(175, "D")
        ;
    }

}