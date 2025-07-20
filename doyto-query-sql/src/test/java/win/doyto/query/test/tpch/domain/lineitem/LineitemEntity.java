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

package win.doyto.query.test.tpch.domain.lineitem;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.ForeignKey;
import win.doyto.query.entity.AbstractPersistable;
import win.doyto.query.test.tpch.domain.orders.OrdersEntity;
import win.doyto.query.test.tpch.domain.part.PartEntity;
import win.doyto.query.test.tpch.domain.partsupp.PartsuppEntity;
import win.doyto.query.test.tpch.domain.supplier.SupplierEntity;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * LineitemEntity
 *
 * @author f0rb on 2023/2/16
 * @since 1.0.1
 */
@Getter
@Setter
public class LineitemEntity extends AbstractPersistable<Long> {

    @ForeignKey(entity = OrdersEntity.class, field = "o_orderkey")
    private Integer l_orderkey;

    @ForeignKey(entity = PartsuppEntity.class, field = "ps_suppkey")
    @ForeignKey(entity = SupplierEntity.class, field = "s_suppkey")
    private Integer l_suppkey;

    @ForeignKey(entity = PartsuppEntity.class, field = "ps_partkey")
    @ForeignKey(entity = PartEntity.class, field = "p_partkey")
    private Integer l_partkey;

    private Integer l_quantity;

    private BigDecimal l_extendedprice;

    private BigDecimal l_discount;

    private BigDecimal l_tax;

    private int l_returnflag;

    private int l_linestatus;

    private Date l_shipdate;

    private Date l_commitdate;

    private Date l_receiptdate;

    private String l_shipinstruct;

    private String l_shipmode;

    private String l_comment;

}
