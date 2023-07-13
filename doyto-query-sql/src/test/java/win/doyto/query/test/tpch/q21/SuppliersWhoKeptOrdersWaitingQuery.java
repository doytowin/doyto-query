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

package win.doyto.query.test.tpch.q21;

import lombok.*;
import lombok.experimental.SuperBuilder;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.core.PageQuery;

/**
 * SuppliersWhoKeptOrdersWaitingQuery
 *
 * @author f0rb on 2023/7/13
 * @since 1.0.2
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SuppliersWhoKeptOrdersWaitingQuery extends PageQuery {
    private String n_name;
    private String o_orderstatus;
    @Builder.Default
    private boolean l1$lReceiptdateGtL1$lCommitdate = true;
    @DomainPath(value = "lineitem", foreignAlias = "l2", foreignField = "l_orderkey",
            localAlias = "l1", localField = "l_orderkey")
    private LineitemExistsQuery lineitemExists;
    @DomainPath(value = "lineitem", foreignAlias = "l3", foreignField = "l_orderkey",
            localAlias = "l1", localField = "l_orderkey")
    private LineitemExistsQuery lineitemNotExists;
}
