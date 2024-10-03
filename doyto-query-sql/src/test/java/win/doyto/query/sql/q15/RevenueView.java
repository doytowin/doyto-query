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

package win.doyto.query.sql.q15;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.Column;
import win.doyto.query.annotation.ForeignKey;
import win.doyto.query.annotation.GroupBy;
import win.doyto.query.annotation.View;
import win.doyto.query.test.tpch.domain.lineitem.LineitemEntity;
import win.doyto.query.test.tpch.domain.supplier.SupplierEntity;

import java.math.BigDecimal;

/**
 * LineitemView
 *
 * @author f0rb on 2023/6/13
 * @since 1.0.2
 */
@Getter
@Setter
@View(LineitemEntity.class)
public class RevenueView {
    @ForeignKey(entity = SupplierEntity.class, field = "s_suppkey")
    @GroupBy
    @Column(name = "l_suppkey")
    private Integer supplier_no;
    @Column(name = "SUM(l_extendedprice * (1 - l_discount))")
    private BigDecimal total_revenue;
}

