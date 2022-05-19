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

package win.doyto.query.mongodb.test.inventory;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.mongodb.entity.MongoPersistable;

import javax.persistence.Entity;
import javax.persistence.EntityType;

/**
 * InventoryEntity
 *
 * @author f0rb on 2021-11-23
 */
@Getter
@Setter
@Entity(type = EntityType.MONGO_DB, database = "doyto", name = "c_inventory")
public class InventoryEntity extends MongoPersistable<String> {

    private String item;
    private Integer qty;
    private InventorySize size;
    private String status;

}
