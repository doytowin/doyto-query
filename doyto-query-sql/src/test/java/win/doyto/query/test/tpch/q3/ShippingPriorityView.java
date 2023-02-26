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

package win.doyto.query.test.tpch.q3;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.GroupBy;
import win.doyto.query.annotation.View;
import win.doyto.query.test.tpch.domain.customer.CustomerEntity;
import win.doyto.query.test.tpch.domain.lineitem.LineitemEntity;
import win.doyto.query.test.tpch.domain.orders.OrdersEntity;

import java.sql.Date;
import javax.persistence.Column;

/**
 * ShippingPriorityView
 *
 * @author f0rb on 2023/2/16
 * @since 1.0.1
 */
@Getter
@Setter
@View({CustomerEntity.class, OrdersEntity.class, LineitemEntity.class})
public class ShippingPriorityView {
    @GroupBy
    private String l_orderkey;
    @Column(name = "SUM(l_extendedprice * (1 - l_discount))")
    private Double revenue;
    @GroupBy
    private Date o_orderdate;
    @GroupBy
    private String o_shippriority;
}
