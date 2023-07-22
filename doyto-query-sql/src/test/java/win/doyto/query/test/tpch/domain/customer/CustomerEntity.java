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

package win.doyto.query.test.tpch.domain.customer;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.ForeignKey;
import win.doyto.query.entity.AbstractPersistable;
import win.doyto.query.test.tpch.domain.nation.NationEntity;

/**
 * CustomerEntity
 *
 * @author f0rb on 2023/2/16
 * @since 1.0.1
 */
@Getter
@Setter
public class CustomerEntity extends AbstractPersistable<Long> {
    private Integer c_custkey;
    private String c_mktsegment;

    @ForeignKey(entity = NationEntity.class, field = "n_nationkey")
    private Integer c_nationkey;
}
