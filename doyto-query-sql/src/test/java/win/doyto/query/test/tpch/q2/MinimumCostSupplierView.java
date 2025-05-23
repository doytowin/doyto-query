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

package win.doyto.query.test.tpch.q2;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.CompositeView;
import win.doyto.query.test.tpch.domain.nation.NationEntity;
import win.doyto.query.test.tpch.domain.part.PartEntity;
import win.doyto.query.test.tpch.domain.partsupp.PartsuppEntity;
import win.doyto.query.test.tpch.domain.region.RegionEntity;
import win.doyto.query.test.tpch.domain.supplier.SupplierEntity;

/**
 * MinimumCostSupplierView
 *
 * @author f0rb on 2023/2/16
 * @since 1.0.1
 */
@Getter
@Setter
@CompositeView({PartEntity.class, SupplierEntity.class, PartsuppEntity.class, NationEntity.class, RegionEntity.class})
public class MinimumCostSupplierView {
    private String s_acctbal;
    private String s_name;
    private String n_name;
    private Integer p_partkey;
    private String p_mfgr;
    private String s_address;
    private String s_phone;
    private String s_comment;
}
