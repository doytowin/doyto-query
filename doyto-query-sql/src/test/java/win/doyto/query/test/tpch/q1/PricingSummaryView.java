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

package win.doyto.query.test.tpch.q1;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.GroupBy;
import win.doyto.query.annotation.View;
import win.doyto.query.test.tpch.domain.lineitem.LineitemEntity;

import java.math.BigDecimal;
import javax.persistence.Column;

/**
 * PricingSummaryView
 *
 * @author f0rb on 2023/2/16
 * @since 1.0.1
 */
@Getter
@Setter
@View(LineitemEntity.class)
public class PricingSummaryView {
    @GroupBy
    private int l_returnflag;
    @GroupBy
    private int l_linestatus;
    @Column(name = "sum(l_quantity)")
    private BigDecimal sum_qty;
    @Column(name = "sum(l_extendedprice)")
    private BigDecimal sum_base_price;
    @Column(name = "sum(l_extendedprice*(1-l_discount))")
    private BigDecimal sum_disc_price;
    @Column(name = "sum(l_extendedprice*(1-l_discount)*(1+l_tax))")
    private BigDecimal sum_charge;
    @Column(name = "avg(l_quantity)")
    private Double avg_qty;
    @Column(name = "avg(l_extendedprice)")
    private Double avg_price;
    @Column(name = "avg(l_discount)")
    private Double avg_disc;
    @Column(name = "count(*)")
    private int count_order;
}
