/*
 * Copyright © 2025 DoytoWin, Inc.
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

package win.doyto.query.jdbc.rowmapper;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.GeneratedValue;
import win.doyto.query.annotation.Id;
import win.doyto.query.entity.Persistable;

@Getter
@Setter
public class RegionEntity implements Persistable<Integer> {
    @Id
    @GeneratedValue
    private Integer regionkey;
    private String name;
    private String comment;

    @Override
    public Integer getId() {
        return this.regionkey;
    }

    @Override
    public void setId(Integer id) {
        this.regionkey = id;
    }
}
