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

package win.doyto.query.mongodb.aggregation;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.AllArgsConstructor;
import org.bson.Document;

import java.util.function.Function;
import javax.persistence.Entity;

/**
 * CollectionProvider
 *
 * @author f0rb on 2022/7/20
 * @since 1.0.0
 */
@AllArgsConstructor
public class CollectionProvider implements Function<Class<?>, MongoCollection<Document>> {
    private MongoClient mongoClient;

    @Override
    public MongoCollection<Document> apply(Class<?> viewClass) {
        Entity mongoEntity = viewClass.getAnnotation(Entity.class);
        MongoDatabase database = mongoClient.getDatabase(mongoEntity.database());
        return database.getCollection(mongoEntity.name());
    }
}
