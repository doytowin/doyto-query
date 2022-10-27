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

package win.doyto.query.mongodb.transaction.spring;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * TransactionTest
 *
 * @author f0rb on 2022-07-19
 */
@ActiveProfiles("test")
@SpringBootTest
class TransactionTest {

    private final ObjectId roleId1 = new ObjectId("6285feedee051b404915c101");

    @Autowired
    private TransactionEnabledService transactionEnabledService;

    @Test
    void commit() throws Exception {
        List<ObjectId> k2List = Arrays.asList(
                new ObjectId("6285feedee051b404915c104"),
                new ObjectId("6285feedee051b404915c105"));
        transactionEnabledService.reassociateForRole(roleId1, k2List);
        assertThat(transactionEnabledService.queryPermsBy(roleId1))
                .containsExactlyInAnyOrderElementsOf(k2List);
    }

    @Test
    void writeConflictAgainstCommit() throws Exception {
        List<ObjectId> k2List = Arrays.asList(
                new ObjectId("6285feedee051b404915c102"),
                new ObjectId("6285feedee051b404915c104"),
                new ObjectId("6285feedee051b404915c106"));
        transactionEnabledService.reassociateForRole(roleId1, k2List);
        assertThat(transactionEnabledService.queryPermsBy(roleId1))
                .containsExactlyInAnyOrderElementsOf(k2List);
    }

    @Test
    void rollback() {
        ObjectId roleId2 = new ObjectId("6285feedee051b404915c102");
        assertThat(transactionEnabledService.queryPermsBy(roleId2)).hasSize(1);
        try {
            transactionEnabledService.reassociateForRole(roleId2, new ArrayList<>());
            fail();
        } catch (Exception e) {// ignore
            assertThat(e.getMessage()).isEqualTo("Perms can not be null.");
        }
        assertThat(transactionEnabledService.queryPermsBy(roleId2)).hasSize(1);
    }
}
