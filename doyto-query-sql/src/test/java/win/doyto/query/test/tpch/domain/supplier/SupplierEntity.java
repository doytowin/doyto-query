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

package win.doyto.query.test.tpch.domain.supplier;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.ForeignKey;
import win.doyto.query.entity.AbstractPersistable;
import win.doyto.query.test.tpch.domain.nation.NationEntity;

/**
 * SupplierEntity
 *
 * @author f0rb on 2023/2/17
 * @since 1.0.1
 */
@Getter
@Setter
public class SupplierEntity extends AbstractPersistable<Long> {
    private Integer s_suppkey;
    @ForeignKey(entity = NationEntity.class, field = "n_nationkey")
    private Integer s_nationkey;
    private String s_comment;
}
