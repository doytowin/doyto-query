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

package win.doyto.query.mongodb.filter;

import lombok.experimental.UtilityClass;
import org.bson.Document;
import org.bson.conversions.Bson;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.util.ColumnUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.mongodb.client.model.Aggregates.*;
import static win.doyto.query.config.GlobalConfiguration.*;
import static win.doyto.query.mongodb.filter.MongoFilterBuilder.buildFilter;

/**
 * DomainPathBuilder
 *
 * @author f0rb on 2022-05-20
 */
@UtilityClass
public class DomainPathBuilder {
    private static final String MONGO_ID = "_id";
    private static final int PROJECTING = 1;

    public static <V> Bson buildLookUpForSubDomain(DoytoQuery query, Class<V> viewClass, Field field) {
        String[] paths = field.getAnnotation(DomainPath.class).value();
        String viewName = field.getName();
        String joinTableName = String.format(JOIN_TABLE_FORMAT, paths[0], paths[1]);

        Document projectDoc = new Document();
        ColumnUtil.filterFields(viewClass).forEach(f -> projectDoc.append(f.getName(), PROJECTING));

        int n = paths.length - 1;
        String[] tableNames = Arrays.stream(paths).map(path -> String.format(TABLE_FORMAT, path)).toArray(String[]::new);
        String[] joinIds = Arrays.stream(paths).map(path -> String.format(JOIN_ID_FORMAT, path)).toArray(String[]::new);

        List<Bson> pipeline = Arrays.asList(
                lookup0(tableNames[n], joinIds[n], MONGO_ID, viewName),
                replaceRoot(new Document("$arrayElemAt", Arrays.asList("$" + viewName, 0))),
                match(buildFilter(query)),
                project(projectDoc)
        );
        return lookup0(joinTableName, MONGO_ID, joinIds[0], pipeline, viewName);
    }

    private static Bson lookup0(String from, String localField, String foreignField, String as) {
        return lookup0(from, localField, foreignField, Collections.emptyList(), as);
    }

    private static Bson lookup0(
            String from, String localField, String foreignField, List<? extends Bson> pipeline, String as
    ) {
        Document lookupDoc = new Document()
                .append("from", from)
                .append("localField", localField)
                .append("foreignField", foreignField);
        if (!pipeline.isEmpty()) {
            lookupDoc.append("pipeline", pipeline);
        }
        lookupDoc.append("as", as);
        return new Document("$lookup", lookupDoc);
    }

}
