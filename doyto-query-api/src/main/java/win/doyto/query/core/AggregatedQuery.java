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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * AggregatedQuery
 *
 * @author f0rb on 2024/8/12
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class AggregatedQuery extends PageQuery implements AggregateQuery {
    private Query query;
    private Having having;
    // AggregateQuery for classes mapping with clause
    private Map<Class<?>, AggregateQuery> withMap = new HashMap<>();

    public AggregatedQuery(PageQuery query) {
        this.query = query;
        this.setPageQuery(query);
    }

    public void setPageQuery(PageQuery pageQuery) {
        if (pageQuery.needPaging()) {
            super.setPageNumber(pageQuery.getPageNumber());
            super.setPageSize(pageQuery.getPageSize());
        }
        super.setSort(pageQuery.getSort());
    }
}
