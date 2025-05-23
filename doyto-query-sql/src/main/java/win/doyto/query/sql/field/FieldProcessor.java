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

package win.doyto.query.sql.field;

import java.util.List;

/**
 * FieldProcessor
 *
 * @author f0rb on 2023/2/19
 * @since 1.0.1
 */
interface FieldProcessor {
    /**
     * Generate SQL condition by provided value.
     *
     * @param alias empty or string ending with dot.
     * @return SQL condition
     */
    String process(String alias, List<Object> argList, Object value);
}
