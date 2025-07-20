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

package win.doyto.query.sql;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import win.doyto.query.core.DoytoQuery;

@Data
@RequiredArgsConstructor
public class WrappedQuery implements DoytoQuery {
    @Delegate
    @NonNull
    private DoytoQuery deleget;
    private transient SqlAndArgs saa;
}
