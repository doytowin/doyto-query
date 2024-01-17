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

package win.doyto.query.dialect;

import java.util.HashSet;
import java.util.Set;

/**
 * MySQL8Dialect
 *
 * @author f0rb on 2020-04-02
 */
public class MySQL8Dialect extends MySQLDialect {

    private final Set<String> keywords;

    public MySQL8Dialect() {
        keywords = new HashSet<>();
        keywords.add("rank");
    }

    @Override
    public String wrapLabel(String fieldName) {
        return keywords.contains(fieldName) ? "`" + fieldName + "`" : fieldName;
    }

    @Override
    public boolean supportMultiGeneratedKeys() {
        return true;
    }

    @Override
    public String resolveKeyColumn(String idColumn) {
        return "GENERATED_KEY";
    }
}
