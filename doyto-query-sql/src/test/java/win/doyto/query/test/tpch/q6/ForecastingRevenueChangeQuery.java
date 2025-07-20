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

package win.doyto.query.test.tpch.q6;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.core.PageQuery;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * ForecastingRevenueChangeQuery
 *
 * @author f0rb on 2023/2/18
 * @since 1.0.1
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastingRevenueChangeQuery extends PageQuery {
    private Date l_shipdateGe;
    private Date l_shipdateLt;
    private BigDecimal l_discountGe;
    private BigDecimal l_discountLe;
    private Integer l_quantityLt;

    public void setBaseShipdate(LocalDate date) {
        l_shipdateGe = Date.valueOf(date);
        l_shipdateLt = Date.valueOf(date.plus(1, ChronoUnit.YEARS));
    }

    public void setBaseDiscount(BigDecimal discount) {
        l_discountGe = discount.subtract(BigDecimal.valueOf(0.01));
        l_discountLe = discount.add(BigDecimal.valueOf(0.01));
    }
}
