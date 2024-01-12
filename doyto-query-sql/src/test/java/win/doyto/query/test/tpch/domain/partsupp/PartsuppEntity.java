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

package win.doyto.query.test.tpch.domain.partsupp;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.Entity;

import java.math.BigDecimal;

/**
 * PartsuppEntity
 *
 * @author f0rb on 2023/2/17
 */
@Getter
@Setter
@Entity(name = "partsupp")
public class PartsuppEntity extends PartsuppKey {
    private Integer ps_availqty;
    private BigDecimal ps_supplycost;
    private String ps_comment;
}
