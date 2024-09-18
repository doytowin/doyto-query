/*
 * Copyright © 2019-2024 Forb Yuan
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

package win.doyto.query.core;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * AggregatePageQuery
 *
 * @author f0rb on 2024/8/12
 */
@SuperBuilder(builderMethodName = "creator")
@NoArgsConstructor
@AllArgsConstructor
public class AggregatePageQuery<Q extends Query, H extends Having> extends PageQuery implements AggregateQuery {
    @Getter
    @Setter
    private Q query;
    @Getter
    @Setter
    private H having;
    // AggregateQuery for classes mapping with clause
    @Builder.Default
    private Map<Class<?>, AggregateQuery> withMap = new HashMap<>();

    public void setPageQuery(DoytoQuery pageQuery) {
        if (pageQuery.needPaging()) {
            super.setPageNumber(pageQuery.getPageNumber());
            super.setPageSize(pageQuery.getPageSize());
        }
        super.setSort(pageQuery.getSort());
        super.setLockMode(pageQuery.getLockMode());
    }

    @Override
    public AggregateQuery get(Class<?> clazz) {
        return withMap.get(clazz);
    }

    public AggregateQuery with(Class<?> clazz, AggregateQuery aggregateQuery) {
        withMap.put(clazz, aggregateQuery);
        return this;
    }
}
