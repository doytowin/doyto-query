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

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;

/**
 * LogProcessor
 *
 * @author f0rb on 2024/7/15
 * @since 1.0.4
 */
@Slf4j
@AllArgsConstructor
public class LogProcessor implements FieldProcessor {
    private final Field field;

    @Override
    public String process(String alias, List<Object> argList, Object value) {
        log.debug("Query field is ignored: {}.{}", field.getDeclaringClass(), field.getName());
        return null;
    }
}
