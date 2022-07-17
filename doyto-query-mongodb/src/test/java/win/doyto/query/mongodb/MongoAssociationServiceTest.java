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
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MongoAssociationServiceTest
 *
 * @author f0rb on 2022-06-17
 */
class MongoAssociationServiceTest extends MongoApplicationTest {

    private final MongoAssociationService associationService;
    private final ObjectId k1Id = new ObjectId("628b3a27f7a4ba009198a677");
    private final ObjectId k2Id = new ObjectId("6285feedee051b404915c103");

    public MongoAssociationServiceTest(@Autowired MongoClient mongoClient) {
        associationService = new MongoAssociationService(mongoClient, "doyto", "user", "role");
    }

    @Test
    void queryK1ByK2() {
        List<ObjectId> k1List = associationService.queryK1ByK2(k2Id);
        assertThat(k1List).containsExactly(
                new ObjectId("628b3a27f7a4ba009198a677"),
                new ObjectId("628b3a27f7a4ba009198a678")
        );
    }

    @Test
    void queryK2ByK1() {
        List<ObjectId> k2List = associationService.queryK2ByK1(k1Id);
        assertThat(k2List).containsExactly(
                new ObjectId("6285feedee051b404915c101"),
                new ObjectId("6285feedee051b404915c102"),
                new ObjectId("6285feedee051b404915c103")
        );
    }

    @Test
    void deleteByK1() {
        int cnt = associationService.deleteByK1(k1Id);
        assertThat(cnt).isEqualTo(3);
    }

    @Test
    void deleteByK2() {
        int cnt = associationService.deleteByK2(k2Id);
        assertThat(cnt).isEqualTo(2);
    }

    @Test
    void associateOne() {
        associationService.associate(k1Id, new ObjectId("6285feedee051b404915c104"));
        assertThat(associationService.queryK2ByK1(k1Id)).hasSize(4);
    }

    @Test
    void dissociateOne() {
        associationService.dissociate(k1Id, new ObjectId("6285feedee051b404915c103"));
        assertThat(associationService.queryK2ByK1(k1Id)).containsExactly(
                new ObjectId("6285feedee051b404915c101"),
                new ObjectId("6285feedee051b404915c102")
        );
    }

    @Test
    void dissociateMulti() {
        associationService.dissociate(associationService.buildUniqueKeys(
                k1Id, Arrays.asList(new ObjectId("6285feedee051b404915c102"),
                                    new ObjectId("6285feedee051b404915c103"))));
        assertThat(associationService.queryK2ByK1(k1Id)).containsExactly(
                new ObjectId("6285feedee051b404915c101")
        );
    }

    @Test
    void reassociateForK1() {
        List<ObjectId> k2List = Arrays.asList(
                new ObjectId("6285feedee051b404915c104"),
                new ObjectId("6285feedee051b404915c105"));
        associationService.reassociateForK1(k1Id, k2List);
        assertThat(associationService.queryK2ByK1(k1Id))
                .containsExactlyInAnyOrderElementsOf(k2List);
    }
}
