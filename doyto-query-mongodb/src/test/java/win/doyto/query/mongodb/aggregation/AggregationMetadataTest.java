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

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;
import win.doyto.query.mongodb.test.role.RoleViewQuery;
import win.doyto.query.mongodb.test.user.UserView;
import win.doyto.query.mongodb.test.user.UserViewQuery;
import win.doyto.query.test.TestQuery;
import win.doyto.query.test.user.UserQuery;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static win.doyto.query.mongodb.test.TestUtil.readString;

/**
 * AggregationMetadataTest
 *
 * @author f0rb on 2022-06-14
 * @since 1.0.0
 */
class AggregationMetadataTest {
    @Test
    void supportRelativeQueryForOneToMany() {
        UserQuery createUserQuery = UserQuery.builder().username("f0rb").build();
        TestQuery query = TestQuery.builder().createUser(createUserQuery).build();
        AggregationMetadata<Object> md = new AggregationMetadata<>(UserView.class, null);

        List<Bson> pipeline = md.buildAggregation(query);

        List<BsonDocument> result = pipeline.stream().map(Bson::toBsonDocument).collect(Collectors.toList());
        BsonArray expected = BsonArray.parse(readString("/query_user_filter_by_create_user.json"));
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void supportRelativeQueryForManyToMany() {
        RoleViewQuery rolesQuery = RoleViewQuery.builder().build();
        UserViewQuery userViewQuery = UserViewQuery.builder().rolesQuery(rolesQuery).build();
        AggregationMetadata<Object> md = new AggregationMetadata<>(UserView.class, null);

        List<Bson> pipeline = md.buildAggregation(userViewQuery);

        List<BsonDocument> result = pipeline.stream().map(Bson::toBsonDocument).collect(Collectors.toList());
        BsonArray expected = BsonArray.parse(readString("/query_user_with_roles.json"));
        assertThat(result).isEqualTo(expected);
    }
}