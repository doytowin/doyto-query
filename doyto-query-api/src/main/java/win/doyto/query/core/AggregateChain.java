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

import java.util.List;

/**
 * AggregateChain
 *
 * @author f0rb on 2024/9/12
 */
public interface AggregateChain<V> {

    AggregateChain<V> where(Query query);

    AggregateChain<V> having(Having having);

    AggregateChain<V> paging(DoytoQuery pageQuery);

    /**
     * Set {@link AggregateQuery} for related with/nested view.
     *
     * @param clazz          with/nested view class.
     * @param aggregateQuery dynamic mapping for with/nested view.
     * @return itself
     */
    AggregateChain<V> with(Class<?> clazz, AggregateQuery aggregateQuery);

    AggregateChain<V> aggregateQuery(AggregateQuery aggregateQuery);

    /**
     * Set custom mapper for view
     *
     * @param mapper custom mapper depending on the implementation
     * @return {@link AggregateChain}
     */
    AggregateChain<V> mapper(Object mapper);

    List<V> query();

    long count();

    default PageList<V> page() {
        return new PageList<>(this.query(), this.count());
    }

    void print();

}
