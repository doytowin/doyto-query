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

package win.doyto.query.test.tpch.q22;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.GroupBy;
import win.doyto.query.annotation.View;
import win.doyto.query.annotation.ViewType;

import javax.persistence.Column;

/**
 * GlobalSalesOpportunityView
 *
 * @author f0rb on 2023/7/13
 * @since 1.0.2
 */
@Getter
@Setter
@View(value = CustsaleView.class, type = ViewType.NESTED)
public class GlobalSalesOpportunityView {
    @GroupBy
    private String cntrycode;
    @Column(name = "count(*)")
    private Long numcust;
    @Column(name = "sum(c_acctbal)")
    private Long totacctbal;
}
