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

package win.doyto.query.test.tpch.domain.partsupp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import win.doyto.query.annotation.ForeignKey;
import win.doyto.query.core.CompositeId;
import win.doyto.query.entity.Persistable;
import win.doyto.query.test.tpch.domain.part.PartEntity;
import win.doyto.query.test.tpch.domain.supplier.SupplierEntity;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * PartsuppKey
 *
 * @author f0rb on 2023/2/24
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PartsuppKey implements CompositeId, Persistable<PartsuppKey> {
    @Id
    @ForeignKey(entity = PartEntity.class, field = "p_partkey")
    private Integer ps_partkey;

    @Id
    @ForeignKey(entity = SupplierEntity.class, field = "s_suppkey")
    private Integer ps_suppkey;

    @Override
    public List<Serializable> toKeys() {
        return Arrays.asList(ps_partkey, ps_suppkey);
    }

    @JsonIgnore
    @Override
    public PartsuppKey getId() {
        return new PartsuppKey(ps_partkey, ps_suppkey);
    }

    @Override
    public void setId(PartsuppKey partsuppKey) {
        this.ps_partkey = partsuppKey.ps_partkey;
        this.ps_suppkey = partsuppKey.ps_suppkey;
    }
}
