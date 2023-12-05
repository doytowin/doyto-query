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
import win.doyto.query.core.PageQuery;

/**
 * LineitemQuery
 *
 * @author f0rb on 2023/7/13
 * @since 1.0.2
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LineitemExistsQuery extends PageQuery {
    @Builder.Default
    private boolean alias$lSuppkeyNeL1$lSuppkey = true;
    private boolean alias$lReceiptdateGtAlias$lCommitdate;
}
