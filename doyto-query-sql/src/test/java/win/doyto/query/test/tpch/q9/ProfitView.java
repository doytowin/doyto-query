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

package win.doyto.query.test.tpch.q9;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.Column;
import win.doyto.query.annotation.CompositeView;
import win.doyto.query.test.tpch.domain.lineitem.LineitemEntity;
import win.doyto.query.test.tpch.domain.nation.NationEntity;
import win.doyto.query.test.tpch.domain.orders.OrdersEntity;
import win.doyto.query.test.tpch.domain.part.PartEntity;
import win.doyto.query.test.tpch.domain.partsupp.PartsuppEntity;
import win.doyto.query.test.tpch.domain.supplier.SupplierEntity;

import java.math.BigDecimal;

/**
 * ProfitView
 *
 * @author f0rb on 2023/2/18
 * @since 1.0.1
 */
@Getter
@Setter
@CompositeView({PartEntity.class, SupplierEntity.class, LineitemEntity.class, PartsuppEntity.class, OrdersEntity.class, NationEntity.class})
public class ProfitView {

    @Column(name = "n_name")
    private String nation;

    @Column(name = "YEAR(o_orderdate)")
    private String o_year;

    @Column(name = "l_extendedprice * (1 - l_discount) - ps_supplycost * l_quantity")
    private BigDecimal amount;

}
