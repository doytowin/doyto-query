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

package win.doyto.query.test.tpch.domain.orders;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.ForeignKey;
import win.doyto.query.entity.AbstractPersistable;
import win.doyto.query.test.tpch.domain.customer.CustomerEntity;

import java.sql.Date;

/**
 * OrdersEntity
 *
 * @author f0rb on 2023/2/16
 * @since 1.0.1
 */
@Getter
@Setter
public class OrdersEntity extends AbstractPersistable<Long> {

    private Integer o_orderkey;

    @ForeignKey(entity = CustomerEntity.class, field = "c_custkey")
    private Integer o_custkey;

    private Date o_orderdate;

    private Integer o_shippriority;

}
