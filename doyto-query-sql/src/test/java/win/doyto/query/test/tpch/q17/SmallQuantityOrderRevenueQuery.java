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

package win.doyto.query.test.tpch.q17;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.annotation.Column;
import win.doyto.query.annotation.NoLabel;
import win.doyto.query.annotation.SubqueryV2;
import win.doyto.query.annotation.View;
import win.doyto.query.core.PageQuery;
import win.doyto.query.test.tpch.domain.lineitem.LineitemEntity;
import win.doyto.query.test.tpch.domain.lineitem.LineitemQuery;
import win.doyto.query.test.tpch.domain.part.PartEntity;

/**
 * SmallQuantityOrderRevenueQuery
 *
 * @author f0rb on 2023/2/19
 * @since 1.0.1
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SmallQuantityOrderRevenueQuery extends PageQuery {
    private String p_brand;
    private String p_container;
    @SubqueryV2(QuantityView.class)
    private LineitemQuery l_quantityLt;

    @View(value = PartEntity.class, context = true)
    @View(LineitemEntity.class)
    private static class QuantityView {
        @NoLabel
        @Column(name = "2e-1 * AVG(l_quantity)")
        private Object quantity;
    }
}
