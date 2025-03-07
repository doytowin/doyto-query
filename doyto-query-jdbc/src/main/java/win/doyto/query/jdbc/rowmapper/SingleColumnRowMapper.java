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

package win.doyto.query.jdbc.rowmapper;

import lombok.AllArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SingleColumnRowMapper
 *
 * @author f0rb on 2022/12/23
 * @since 1.0.0
 */
@AllArgsConstructor
public class SingleColumnRowMapper<V> implements RowMapper<V> {
    private Class<V> clazz;

    @Override
    public V map(ResultSet rs, int rn) throws SQLException {
        return rs.getObject(1, clazz);
    }
}
