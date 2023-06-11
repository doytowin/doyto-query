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

package win.doyto.query.test.tpch.q8;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.GroupBy;
import win.doyto.query.annotation.NestedView;

import java.math.BigDecimal;
import javax.persistence.Column;

/**
 * NationalMarketShareView
 *
 * @author f0rb on 2023/6/11
 * @since 1.0.2
 */
@Getter
@Setter
@NestedView(AllNationsView.class)
public class NationalMarketShareView {
    @GroupBy
    private String o_year;
    @Column(name = "SUM(CASE WHEN nation = #{n_name} THEN volume ELSE 0 END) / SUM(volume)")
    private BigDecimal mkt_share;
}
