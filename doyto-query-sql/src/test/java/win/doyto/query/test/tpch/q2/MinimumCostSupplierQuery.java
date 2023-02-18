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

package win.doyto.query.test.tpch.q2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.annotation.Subquery;
import win.doyto.query.core.PageQuery;
import win.doyto.query.test.tpch.domain.nation.NationEntity;
import win.doyto.query.test.tpch.domain.partsupp.PartsuppEntity;
import win.doyto.query.test.tpch.domain.region.RegionEntity;
import win.doyto.query.test.tpch.domain.supplier.SupplierEntity;

/**
 * MinimumCostSupplierQuery
 *
 * @author f0rb on 2023/2/16
 * @since 1.0.1
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MinimumCostSupplierQuery extends PageQuery {
    private Integer p_size;
    private String p_typeEnd;
    private String r_name;
    @Subquery(select = "MIN(ps_supplycost)", parentColumns = {"p_partkey"},
            from = {PartsuppEntity.class, SupplierEntity.class, NationEntity.class, RegionEntity.class})
    private SupplyCostQuery ps_supplycost;
}
