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

import org.junit.jupiter.api.Test;
import win.doyto.query.test.TestEntity;
import win.doyto.query.test.TestEnum;
import win.doyto.query.test.user.UserEntity;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * BeanPropertyRowMapperTest
 *
 * @author f0rb on 2023/10/3
 * @since 1.0.3
 */
class BeanPropertyRowMapperTest {

    @Test
    void supportEnumOrdinal() throws Exception {
        BeanPropertyRowMapper<UserEntity> rowMapper = new BeanPropertyRowMapper<>(UserEntity.class);
        ResultSet rs = mock(ResultSet.class);
        when(rs.getObject("userLevel")).thenReturn("1");
        when(rs.getString("userLevel")).thenReturn("1");
        PropertyDescriptor pd = new PropertyDescriptor(
                "userLevel", TestEntity.class, "getUserLevel", "setUserLevel");

        Object columnValue = rowMapper.getColumnValue(rs, pd, pd.getName());
        assertThat(columnValue).isEqualTo(TestEnum.NORMAL);
    }
}