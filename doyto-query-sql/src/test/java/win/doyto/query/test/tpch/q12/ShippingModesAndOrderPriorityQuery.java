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

package win.doyto.query.test.tpch.q12;

import lombok.*;
import lombok.experimental.SuperBuilder;
import win.doyto.query.core.PageQuery;

import java.sql.Date;
import java.util.List;
import javax.persistence.Transient;

/**
 * ShippingModesAndOrderPriorityQuery
 *
 * @author f0rb on 2023/2/18
 * @since 1.0.1
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingModesAndOrderPriorityQuery extends PageQuery {
    @Transient
    private String o_orderpriority1;
    @Transient
    private String o_orderpriority2;

    private List<String> l_shipmodeIn;

    @Builder.Default
    private boolean l_commitdateLtL_receiptdate = true;

    @Builder.Default
    private boolean l_shipdateLtL_commitdate = true;

    private Date l_receiptdateGe;
    private Date l_receiptdateLt;
}
