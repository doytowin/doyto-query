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
 * AggregateClient
 *
 * @author f0rb on 2024/8/12
 */
public interface AggregateClient {
    <V> AggregateChain<V> aggregate(Class<V> viewClass);

    default <V> List<V> query(Class<V> viewClass, DoytoQuery query) {
        return aggregate(viewClass).filter(query).query();
    }

    default <V> long count(Class<V> viewClass, DoytoQuery query) {
        return aggregate(viewClass).filter(query).count();
    }

    default <V> PageList<V> page(Class<V> viewClass, DoytoQuery query) {
        return aggregate(viewClass).filter(query).page();
    }
}
