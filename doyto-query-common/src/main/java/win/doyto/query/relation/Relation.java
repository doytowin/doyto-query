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

package win.doyto.query.relation;

import lombok.AllArgsConstructor;
import lombok.Data;
import win.doyto.query.config.GlobalConfiguration;

/**
 * Relation
 *
 * @author f0rb on 2024/11/24
 */
@Data
@AllArgsConstructor
public class Relation {
    private String fk1;
    private String associativeTable;
    private String fk2;

    public Relation(String e1, String e2) {
        GlobalConfiguration conf = GlobalConfiguration.instance();
        this.fk1 = conf.formatJoinId(e1);
        this.fk2 = conf.formatJoinId(e2);
        this.associativeTable = conf.formatJoinTable(e1, e2);
    }
}
