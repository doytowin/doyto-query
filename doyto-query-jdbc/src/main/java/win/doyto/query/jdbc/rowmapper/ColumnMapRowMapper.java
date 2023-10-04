/*
 * Copyright © 2019-2023 Forb Yuan
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

import org.springframework.util.LinkedCaseInsensitiveMap;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * ColumnMapRowMapper
 *
 * @author f0rb on 2022/12/23
 * @since 1.0.0
 */
public class ColumnMapRowMapper implements RowMapper<Map<String, Object>> {
    @Override
    public Map<String, Object> map(ResultSet rs, int rn) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        Map<String, Object> mapOfColumnValues = new LinkedCaseInsensitiveMap<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            String column = rsmd.getColumnLabel(i);
            mapOfColumnValues.putIfAbsent(column, rs.getObject(i));
        }
        return mapOfColumnValues;
    }
}
