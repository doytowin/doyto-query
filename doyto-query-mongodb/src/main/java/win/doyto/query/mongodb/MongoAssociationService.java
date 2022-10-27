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
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.AssociationService;
import win.doyto.query.core.UniqueKey;
import win.doyto.query.mongodb.session.MongoSessionSupplier;
import win.doyto.query.mongodb.session.MongoSessionThreadLocalSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * MongoAssociationService
 *
 * @author f0rb on 2022-06-17
 */
public class MongoAssociationService implements AssociationService<ObjectId, ObjectId> {
    private final MongoCollection<Document> collection;
    private final MongoSessionSupplier mongoSessionSupplier;
    private final String domainId1;
    private final String domainId2;

    public MongoAssociationService(MongoClient mongoClient,String database, String domain1, String domain2) {
        this(MongoSessionThreadLocalSupplier.create(mongoClient), database, domain1, domain2);
    }

    public MongoAssociationService(MongoSessionSupplier mongoSessionSupplier, String database, String domain1, String domain2) {
        String joinTableFormat = GlobalConfiguration.instance().getJoinTableFormat();
        String joinTable = String.format(joinTableFormat, domain1, domain2);
        this.collection = mongoSessionSupplier.getMongoClient().getDatabase(database).getCollection(joinTable);
        this.mongoSessionSupplier = mongoSessionSupplier;
        String joinIdFormat = GlobalConfiguration.instance().getJoinIdFormat();
        this.domainId1 = String.format(joinIdFormat, domain1);
        this.domainId2 = String.format(joinIdFormat, domain2);
    }

    @Override
    public int associate(Set<UniqueKey<ObjectId, ObjectId>> uniqueKeys) {
        if (uniqueKeys.isEmpty()) {
            return 0;
        }
        List<Document> list = new ArrayList<>(uniqueKeys.size());
        for (UniqueKey<ObjectId, ObjectId> uniqueKey : uniqueKeys) {
            Document doc = new Document(domainId1, uniqueKey.getK1()).append(domainId2, uniqueKey.getK2());
            list.add(doc);
        }
        return collection.insertMany(mongoSessionSupplier.get(), list).getInsertedIds().size();
    }

    @Override
    public int dissociate(Set<UniqueKey<ObjectId, ObjectId>> uniqueKeys) {
        Bson filter = buildKeysFilter(uniqueKeys);
        return (int) collection.deleteMany(mongoSessionSupplier.get(), filter).getDeletedCount();
    }

    private Bson buildKeysFilter(Set<UniqueKey<ObjectId, ObjectId>> uniqueKeys) {
        List<Bson> filters = new ArrayList<>(uniqueKeys.size());
        for (UniqueKey<ObjectId, ObjectId> uniqueKey : uniqueKeys) {
            Document doc = new Document(domainId1, uniqueKey.getK1()).append(domainId2, uniqueKey.getK2());
            filters.add(doc);
        }
        return Filters.or(filters);
    }

    @Override
    public List<ObjectId> queryK1ByK2(ObjectId k2) {
        return collection.find(mongoSessionSupplier.get(), new Document(domainId2, k2))
                         .map(document -> (ObjectId) document.get(domainId1))
                         .into(new ArrayList<>());
    }

    @Override
    public List<ObjectId> queryK2ByK1(ObjectId k1) {
        return collection.find(mongoSessionSupplier.get(), new Document(domainId1, k1))
                         .map(document -> (ObjectId) document.get(domainId2))
                         .into(new ArrayList<>());
    }

    @Override
    public int deleteByK1(ObjectId k1) {
        return (int) collection.deleteMany(mongoSessionSupplier.get(), new Document(domainId1, k1))
                               .getDeletedCount();
    }

    @Override
    public int deleteByK2(ObjectId k2) {
        return (int) collection.deleteMany(mongoSessionSupplier.get(), new Document(domainId2, k2))
                               .getDeletedCount();
    }

    @Override
    public long count(Set<UniqueKey<ObjectId, ObjectId>> uniqueKeys) {
        Bson filter = buildKeysFilter(uniqueKeys);
        return collection.countDocuments(mongoSessionSupplier.get(), filter);
    }
}
