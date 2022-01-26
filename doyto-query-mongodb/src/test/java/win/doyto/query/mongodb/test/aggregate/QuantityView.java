/*
 * Copyright © 2019-2022 Forb Yuan
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

package win.doyto.query.mongodb.test.aggregate;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.entity.MongoEntity;

/**
 * QuantityView
 *
 * @author f0rb on 2022-01-25
 */
@Getter
@Setter
@MongoEntity(database = "doyto", collection = "c_inventory")
public class QuantityView {
    private Integer sumQty;
    private Integer maxQty;
    private Integer minQty;
    private Double avgQty;
    private Integer firstQty;
    private Integer lastQty;
}
