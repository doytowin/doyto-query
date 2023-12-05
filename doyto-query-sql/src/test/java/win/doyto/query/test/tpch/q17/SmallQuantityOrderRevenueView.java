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

package win.doyto.query.test.tpch.q17;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.Column;
import win.doyto.query.annotation.CompositeView;
import win.doyto.query.test.tpch.domain.lineitem.LineitemEntity;
import win.doyto.query.test.tpch.domain.part.PartEntity;

import java.math.BigDecimal;

/**
 * SmallQuantityOrderRevenueView
 *
 * @author f0rb on 2023/2/19
 * @since 1.0.1
 */
@Getter
@Setter
@CompositeView({LineitemEntity.class, PartEntity.class})
public class SmallQuantityOrderRevenueView {
    @Column(name = "SUM(l_extendedprice) / 7.0")
    private BigDecimal avg_yearly;
}
