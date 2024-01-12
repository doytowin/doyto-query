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

package win.doyto.query.test.tpch.q22;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.annotation.Column;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.annotation.Subquery;
import win.doyto.query.core.PageQuery;
import win.doyto.query.test.tpch.domain.customer.CustomerEntity;

import java.util.List;

/**
 * CustsaleQuery
 *
 * @author f0rb on 2023/7/13
 * @since 1.0.2
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CustsaleQuery extends PageQuery {
    private Integer c_acctbalGt;
    // @QueryField(and = "substring(c_phone from 1 for 2)")
    @Column(name = "substring(c_phone from 1 for 2)")
    private List<String> cntrycodeIn;
    @Subquery(select = "avg(c_acctbal)", from = CustomerEntity.class)
    private CustsaleQuery c_acctbalGt2;
    @DomainPath(value = "orders",
            localAlias = "", localField = "c_custkey",
            foreignAlias = "", foreignField = "o_custkey")
    private PageQuery ordersNotExists;
}
