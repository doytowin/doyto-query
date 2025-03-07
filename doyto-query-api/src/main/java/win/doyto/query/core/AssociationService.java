/*
 * Copyright © 2019-2025 DoytoWin, Inc.
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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AssociationService
 *
 * @author f0rb on 2021-12-18
 */
public interface AssociationService<K1, K2> {

    default Set<UniqueKey<K1, K2>> buildUniqueKeys(K1 k1, List<K2> collection) {
        return collection.stream().map(k2 -> new UniqueKey<>(k1, k2)).collect(Collectors.toSet());
    }

    default Set<UniqueKey<K1, K2>> buildUniqueKeys(List<K1> list, K2 k2) {
        return list.stream().map(k1 -> new UniqueKey<>(k1, k2)).collect(Collectors.toSet());
    }

    default int associate(K1 k1, K2 k2) {
        return associate(Collections.singleton(new UniqueKey<>(k1, k2)));
    }

    int associate(Set<UniqueKey<K1, K2>> uniqueKeys);

    default int dissociate(K1 k1, K2 k2) {
        return dissociate(Collections.singleton(new UniqueKey<>(k1, k2)));
    }

    int dissociate(Set<UniqueKey<K1, K2>> uniqueKeys);

    List<K1> queryK1ByK2(K2 k2);

    List<K2> queryK2ByK1(K1 k1);

    int deleteByK1(K1 k1);

    int deleteByK2(K2 k2);

    default int reassociateForK1(K1 k1, List<K2> list) {
        deleteByK1(k1);
        return associate(buildUniqueKeys(k1, list));
    }

    default int reassociateForK2(K2 k2, List<K1> list) {
        deleteByK2(k2);
        return associate(buildUniqueKeys(list, k2));
    }

    long count(Set<UniqueKey<K1, K2>> set);

    default boolean exists(K1 k1, K2 k2) {
        return exists(Collections.singleton(new UniqueKey<>(k1, k2)));
    }

    default boolean exists(Set<UniqueKey<K1, K2>> uniqueKeys) {
        return count(uniqueKeys) > 0;
    }

    default boolean existsExactly(Set<UniqueKey<K1, K2>> uniqueKeys) {
        return count(uniqueKeys) == uniqueKeys.size();
    }
}
