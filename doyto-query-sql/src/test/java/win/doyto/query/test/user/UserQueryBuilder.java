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

package win.doyto.query.test.user;

import win.doyto.query.core.DoytoQuery;
import win.doyto.query.sql.BuildHelper;
import win.doyto.query.sql.CrudBuilder;

import java.util.List;
import java.util.StringJoiner;

import static win.doyto.query.sql.Constant.*;

/**
 * UserQueryBuilder
 *
 * @author f0rb on 2023/1/29
 * @since 1.0.1
 */
public class UserQueryBuilder extends CrudBuilder<UserEntity> {
    public UserQueryBuilder(Class<UserEntity> entityClass) {
        super(entityClass);
    }

    @Override
    protected String buildWhere(DoytoQuery query, List<Object> argList) {
        UserQuery userQuery = (UserQuery) query;
        StringJoiner whereJoiner = new StringJoiner(AND);
        if (userQuery.getId() != null) {
            whereJoiner.add("id = ?");
            argList.add(userQuery.getId());
        }
        if (userQuery.getIdIn() != null) {
            String holders = "(null)";
            if (!userQuery.getIdIn().isEmpty()) {
                holders = BuildHelper.buildPlaceHolders(userQuery.getIdIn().size());
            }
            whereJoiner.add("id IN " + holders);
            argList.addAll(userQuery.getIdIn());
        }
        if (userQuery.getMobile() != null) {
            whereJoiner.add("mobile = ?");
            argList.add(userQuery.getMobile());
        }
        if (userQuery.getMemoNull()) {
            whereJoiner.add("memo IS NULL");
        }
        return whereJoiner.length() == 0 ? EMPTY : WHERE + whereJoiner;
    }
}
