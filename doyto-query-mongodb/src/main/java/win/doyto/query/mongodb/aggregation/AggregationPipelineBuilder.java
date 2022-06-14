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

import com.mongodb.client.model.Aggregates;
import lombok.experimental.UtilityClass;
import org.bson.Document;
import org.bson.conversions.Bson;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.AggregationQuery;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.Having;
import win.doyto.query.mongodb.filter.MongoFilterBuilder;
import win.doyto.query.util.ColumnUtil;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static win.doyto.query.mongodb.MongoConstant.MONGO_ID;
import static win.doyto.query.mongodb.aggregation.DomainPathBuilder.buildLookUpForSubDomain;

/**
 * AggregationPipelineBuilder
 *
 * @author f0rb on 2022-06-14
 */
@UtilityClass
public class AggregationPipelineBuilder {
    private static final Document SORT_BY_ID = new Document(MONGO_ID, 1);

    public <V, Q extends DoytoQuery> List<Bson> build(Q query, Class<V> viewClass, AggregationMetadata md) {
        List<Bson> list = new ArrayList<>();

        List<String> unsetFields = new ArrayList<>();

        Field[] fields = ColumnUtil.initFields(query.getClass());
        for (Field field : fields) {
            if (field.isAnnotationPresent(DomainPath.class)) {
                Object value = CommonUtil.readFieldGetter(field, query);
                if (value instanceof DoytoQuery) {
                    String subDomainName = field.getName();
                    String[] paths = field.getAnnotation(DomainPath.class).value();
                    Bson lookupDoc = DomainPathBuilder.buildLookUpForNestedQuery(subDomainName, paths);
                    list.add(lookupDoc);

                    unsetFields.add(subDomainName);
                }
            }
        }
        list.add(Aggregates.match(MongoFilterBuilder.buildFilter(query)));
        if (!unsetFields.isEmpty()) {
            list.add(new Document("$unset", unsetFields));
        }

        for (Field field : md.getDomainFields()) {
            Object domainQuery = CommonUtil.readField(query, field.getName() + "Query");
            if (domainQuery instanceof DoytoQuery) {
                Bson lookupDoc = buildLookUpForSubDomain((DoytoQuery) domainQuery, viewClass, field);
                list.add(lookupDoc);
            }
        }
        if (md.getGroupBy() != null) {
            list.add(md.getGroupBy());
        }
        if (query instanceof AggregationQuery) {
            Having having = ((AggregationQuery) query).getHaving();
            if (having != null) {
                list.add(buildHaving(having));
            }
        }
        list.add(buildSort(query, md.getGroupId().keySet()));
        if (query.needPaging()) {
            list.add(Aggregates.skip(GlobalConfiguration.calcOffset(query)));
            list.add(Aggregates.limit(query.getPageNumber()));
        }
        list.add(md.getProject());
        return list;
    }

    private <H extends Having> Bson buildHaving(H having) {
        return Aggregates.match(MongoFilterBuilder.buildFilter(having));
    }

    private <Q extends DoytoQuery> Bson buildSort(Q query, Set<String> groupColumns) {
        Bson sort = SORT_BY_ID;
        if (query.getSort() != null) {
            sort = MongoFilterBuilder.buildSort(query.getSort(), groupColumns);
        }
        return Aggregates.sort(sort);
    }
}
