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

package win.doyto.query.test.tpch.q8;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.Column;
import win.doyto.query.annotation.View;
import win.doyto.query.test.tpch.domain.customer.CustomerEntity;
import win.doyto.query.test.tpch.domain.lineitem.LineitemEntity;
import win.doyto.query.test.tpch.domain.nation.NationEntity;
import win.doyto.query.test.tpch.domain.orders.OrdersEntity;
import win.doyto.query.test.tpch.domain.part.PartEntity;
import win.doyto.query.test.tpch.domain.region.RegionEntity;
import win.doyto.query.test.tpch.domain.supplier.SupplierEntity;

import java.math.BigDecimal;

/**
 * AllNationsView
 *
 * @author f0rb on 2023/6/11
 * @since 1.0.2
 */
@Getter
@Setter
@View(PartEntity.class)
@View(LineitemEntity.class)
@View(OrdersEntity.class)
@View(CustomerEntity.class)
@View(SupplierEntity.class)
@View(value = NationEntity.class, alias = "n1")
@View(value = NationEntity.class, alias = "n2")
@View(RegionEntity.class)
public class AllNationsView {
    @Column(name = "YEAR(o_orderdate)")
    private String o_year;
    @Column(name = "l_extendedprice * (1 - l_discount)")
    private BigDecimal volume;
    @Column(name = "n2.n_name")
    private String nation;
}
