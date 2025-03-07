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

package win.doyto.query.relation;

import lombok.AllArgsConstructor;
import lombok.Data;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.util.ColumnUtil;

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

    public static Relation build(String e1, String e2) {
        if (e1.contains("->")) {
            String[] entityFk = e1.split("->");
            return new Relation("id", GlobalConfiguration.formatTable(entityFk[0]), ColumnUtil.convertColumn(entityFk[1]));
        } else if (e2.contains("<-")) {
            String[] entityFk = e2.split("<-");
            return new Relation(ColumnUtil.convertColumn(entityFk[1]), GlobalConfiguration.formatTable(entityFk[0]), "id");
        } else if (e1.contains("<-")) {
            e1 = e1.substring(0, e1.indexOf("<-"));
        } else if (e2.contains("->")) {
            e2 = e2.substring(0, e2.indexOf("->"));
        }
        return new Relation(e1, e2);
    }
}
