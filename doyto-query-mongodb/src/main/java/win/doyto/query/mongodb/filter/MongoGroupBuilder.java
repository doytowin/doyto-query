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

import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.BsonField;
import lombok.experimental.UtilityClass;
import win.doyto.query.mongodb.AggregationPrefix;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;

import static win.doyto.query.mongodb.AggregationPrefix.*;

/**
 * MongoGroupBuilder
 *
 * @author f0rb on 2022-01-26
 */

@UtilityClass
public class MongoGroupBuilder {

    private static final Map<AggregationPrefix, BiFunction<String, String, BsonField>> prefixFuncMap;

    static {
        prefixFuncMap = new EnumMap<>(AggregationPrefix.class);
        prefixFuncMap.put(SUM, Accumulators::sum);
        prefixFuncMap.put(MAX, Accumulators::max);
        prefixFuncMap.put(MIN, Accumulators::min);
        prefixFuncMap.put(AVG, Accumulators::avg);
    }

    public static BsonField getBsonField(String viewFieldName) {
        AggregationPrefix aggregationPrefix = AggregationPrefix.resolveField(viewFieldName);
        String fieldName = "$" + aggregationPrefix.resolveColumnName(viewFieldName);
        return build(viewFieldName, aggregationPrefix, fieldName);
    }

    private BsonField build(String viewFieldName, AggregationPrefix aggregationPrefix, String fieldName) {
        return prefixFuncMap.get(aggregationPrefix).apply(viewFieldName, fieldName);
    }
}
