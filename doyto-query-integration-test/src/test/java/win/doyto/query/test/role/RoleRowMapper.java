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

package win.doyto.query.test.role;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RoleRowMapper
 *
 * @author f0rb on 2024/7/30
 * @since 1.0.4
 */
@SuppressWarnings("unused")
public class RoleRowMapper implements RowMapper<RoleEntity> {
    @Override
    public RoleEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(rs.getInt("id"));
        roleEntity.setRoleName(rs.getString("roleName"));
        roleEntity.setRoleCode(rs.getString("roleCode"));
        roleEntity.setValid(rs.getBoolean("valid"));
        return roleEntity;
    }
}
