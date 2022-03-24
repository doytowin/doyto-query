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
import org.apache.commons.lang3.reflect.FieldUtils;
import org.bson.Document;
import win.doyto.query.mongodb.AggregationPrefix;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
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
        prefixFuncMap.put(sum, Accumulators::sum);
        prefixFuncMap.put(max, Accumulators::max);
        prefixFuncMap.put(min, Accumulators::min);
        prefixFuncMap.put(avg, Accumulators::avg);
        prefixFuncMap.put(first, Accumulators::first);
        prefixFuncMap.put(last, Accumulators::last);
        prefixFuncMap.put(stdDevPop, Accumulators::stdDevPop);
        prefixFuncMap.put(stdDevSamp, Accumulators::stdDevSamp);
        prefixFuncMap.put(addToSet, Accumulators::addToSet);
    }

    public static BsonField getBsonField(Field field) {
        String viewFieldName = field.getName();
        AggregationPrefix aggregationPrefix = AggregationPrefix.resolveField(viewFieldName);
        if (aggregationPrefix == push) {
            return buildPushField(field);
        }
        String fieldName = "$" + aggregationPrefix.resolveColumnName(viewFieldName);
        return build(viewFieldName, aggregationPrefix, fieldName);
    }

    private static BsonField buildPushField(Field field) {
        Document expression = new Document();
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        for (Field subField : FieldUtils.getAllFields((Class<?>) type.getActualTypeArguments()[0])) {
            String subFieldName = subField.getName();
            expression.put(subFieldName, "$" + subFieldName);
        }
        return Accumulators.push(field.getName(), expression);
    }

    private BsonField build(String viewFieldName, AggregationPrefix aggregationPrefix, String fieldName) {
        return prefixFuncMap.getOrDefault(aggregationPrefix, (v, t) -> null)
                            .apply(viewFieldName, fieldName);
    }
}
