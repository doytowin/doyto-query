/*
 * Copyright © 2019-2021 Forb Yuan
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

package win.doyto.query.jdbc;

import win.doyto.query.sql.UniqueKey;

import java.util.List;

/**
 * AssociationService
 *
 * @author f0rb on 2021-12-18
 */
public interface AssociationService<K1, K2> {

    int associate(K1 k1, K2 k2);

    int dissociate(K1 k1, K2 k2);

    List<K1> queryK1ByK2(K2 k2);

    List<K2> queryK2ByK1(K1 k1);

    int deleteByK1(K1 k1);

    int deleteByK2(K2 k2);

    int reassociateForK1(K1 k1, List<K2> list);

    int reassociateForK2(K2 k2, List<K1> list);

    long count(List<UniqueKey<K1, K2>> list);
}
