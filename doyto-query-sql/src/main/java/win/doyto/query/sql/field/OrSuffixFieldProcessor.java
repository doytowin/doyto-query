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

package win.doyto.query.sql.field;

import java.util.List;

import static win.doyto.query.sql.Constant.*;

public class OrSuffixFieldProcessor extends ConnectableFieldProcessor {
    public OrSuffixFieldProcessor(Class<?> fieldType) {
        super(fieldType, OR);
    }

    @Override
    public String process(String alias, List<Object> argList, Object value) {
        String clause = super.process(alias, argList, value);
        return clause == null ? null : OP + clause + CP;
    }
}
