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

package win.doyto.query.test.tpch.q5;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.Column;
import win.doyto.query.annotation.CompositeView;
import win.doyto.query.annotation.GroupBy;
import win.doyto.query.test.tpch.domain.customer.CustomerEntity;
import win.doyto.query.test.tpch.domain.lineitem.LineitemEntity;
import win.doyto.query.test.tpch.domain.nation.NationEntity;
import win.doyto.query.test.tpch.domain.orders.OrdersEntity;
import win.doyto.query.test.tpch.domain.region.RegionEntity;
import win.doyto.query.test.tpch.domain.supplier.SupplierEntity;

import java.math.BigDecimal;

/**
 * LocalSupplierVolumeView
 *
 * @author f0rb on 2023/2/18
 * @since 1.0.1
 */
@Getter
@Setter
@CompositeView({CustomerEntity.class, OrdersEntity.class, LineitemEntity.class, SupplierEntity.class, NationEntity.class, RegionEntity.class})
public class LocalSupplierVolumeView {
    @GroupBy
    private String n_name;
    @Column(name = "SUM(l_extendedprice * (1 - l_discount))")
    private BigDecimal revenue;
}
