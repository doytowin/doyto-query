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

import com.mongodb.client.MongoClient;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import win.doyto.query.mongodb.MongoAssociationService;

import java.util.List;

/**
 * TransactionEnabledService
 *
 * @author f0rb on 2022-07-19
 */
@Service
@MongoTransactional
public class TransactionEnabledService {

    private final MongoAssociationService associationService;

    public TransactionEnabledService(@Autowired MongoClient mongoClient) {
        associationService = new MongoAssociationService(mongoClient, "doyto", "role", "perm");
    }

    public void reassociateForRole(ObjectId k1, List<ObjectId> k2List) throws Exception {
        int cnt = associationService.reassociateForK1(k1, k2List);
        if (cnt <= 0) {
            throw new Exception("Perms can not be null.");
        }
    }

    public List<ObjectId> queryPermsBy(ObjectId roleId) {
        return associationService.queryK2ByK1(roleId);
    }

}
