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

package win.doyto.query.test.tpch.q7;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.View;
import win.doyto.query.test.tpch.domain.customer.CustomerEntity;
import win.doyto.query.test.tpch.domain.lineitem.LineitemEntity;
import win.doyto.query.test.tpch.domain.nation.NationEntity;
import win.doyto.query.test.tpch.domain.orders.OrdersEntity;
import win.doyto.query.test.tpch.domain.supplier.SupplierEntity;

import javax.persistence.Column;
import java.math.BigDecimal;

/**
 * AnnualVolumeView
 *
 * @author f0rb on 2023/6/10
 * @since 1.0.2
 */
@Getter
@Setter
@View(SupplierEntity.class)
@View(LineitemEntity.class)
@View(OrdersEntity.class)
@View(CustomerEntity.class)
@View(value = NationEntity.class, alias = "n1")
@View(value = NationEntity.class, alias = "n2")
public class ShippingView {
    @Column(name = "n1.n_name")
    private String supp_nation;
    @Column(name = "n2.n_name")
    private String cust_nation;
    @Column(name = "YEAR(l_shipdate)")
    private String l_year;
    @Column(name = "l_extendedprice * (1 - l_discount)")
    private BigDecimal volume;
}
