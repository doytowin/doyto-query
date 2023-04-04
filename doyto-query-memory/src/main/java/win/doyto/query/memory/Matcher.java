/*
 * Copyright © 2019-2023 Forb Yuan
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

package win.doyto.query.memory;

import java.util.Collection;

/**
 * Matcher
 *
 * @author f0rb on 2021-12-10
 */
interface Matcher {

    /**
     * 实体对象筛选
     *
     * @param qv 查询对象字段值
     * @param ev 实体对象字段值
     * @return true 符合过滤条件
     */
    boolean doMatch(Object qv, Object ev);

    default boolean match(Object qv, Object ev) {
        return isComparable(qv, ev) && doMatch(qv, ev);
    }

    default boolean isComparable(Object qv, Object ev) {
        return qv instanceof Collection || (qv instanceof Comparable && ev instanceof Comparable);
    }
}
