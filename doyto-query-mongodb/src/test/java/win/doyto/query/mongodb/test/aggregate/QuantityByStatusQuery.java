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

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import win.doyto.query.core.JoinQuery;
import win.doyto.query.core.PageQuery;

/**
 * QuantityByStatusQuery
 *
 * @author f0rb on 2022-05-11
 */
@SuperBuilder
@NoArgsConstructor
public class QuantityByStatusQuery extends PageQuery implements JoinQuery<QuantityByStatusView, String> {
    @Override
    public Class<QuantityByStatusView> getDomainClass() {
        return QuantityByStatusView.class;
    }
}
