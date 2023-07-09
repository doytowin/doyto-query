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

package win.doyto.query.test.tpch.q5;

import lombok.*;
import lombok.experimental.SuperBuilder;
import win.doyto.query.core.PageQuery;

import java.sql.Date;

/**
 * LocalSupplierVolumeQuery
 *
 * @author f0rb on 2023/2/18
 * @since 1.0.1
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LocalSupplierVolumeQuery extends PageQuery {
    @Builder.Default
    private boolean cNationkeyEqSNationkey = true;
    private String rName;
    private Date oOrderdateGe;
    private Date oOrderdateLt;
}
