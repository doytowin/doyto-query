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

package win.doyto.query.sql;

import org.junit.jupiter.api.Test;
import win.doyto.query.test.DynamicEntity;
import win.doyto.query.test.DynamicIdWrapper;
import win.doyto.query.test.DynamicQuery;
import win.doyto.query.test.tpch.domain.partsupp.PartsuppEntity;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * SqlBuilderTest
 *
 * @author f0rb on 2021-12-10
 */
class SqlBuilderTest {

    private SqlBuilder<DynamicEntity> sqlBuilder = new CrudBuilder<>(DynamicEntity.class);

    @Test
    void buildDeleteByIdIn() {
        DynamicIdWrapper idWrapper = new DynamicIdWrapper();
        idWrapper.setId(1);
        idWrapper.setUser("f0rb");
        idWrapper.setProject("i18n");

        SqlAndArgs sqlAndArgs = sqlBuilder.buildDeleteByIdIn(idWrapper, Arrays.asList(1, 2, 3));
        assertEquals("DELETE FROM t_dynamic_f0rb_i18n WHERE id IN (?, ?, ?)", sqlAndArgs.getSql());
        assertEquals(3, sqlAndArgs.getArgs().length);
    }

    @Test
    void buildDeleteByIdInWithEmptyIds() {
        DynamicIdWrapper idWrapper = new DynamicIdWrapper();
        idWrapper.setId(1);
        idWrapper.setUser("f0rb");
        idWrapper.setProject("i18n");

        SqlAndArgs sqlAndArgs = sqlBuilder.buildDeleteByIdIn(idWrapper, Arrays.asList());
        assertEquals("DELETE FROM t_dynamic_f0rb_i18n WHERE id IN (null)", sqlAndArgs.getSql());
        assertEquals(0, sqlAndArgs.getArgs().length);
    }

    @Test
    void buildPatchAndArgsWithIds() {
        DynamicEntity entity = new DynamicEntity();
        entity.setUser("f0rb");
        entity.setProject("i18n");
        entity.setScore(100);

        SqlAndArgs sqlAndArgs = sqlBuilder.buildPatchAndArgsWithIds(entity, Arrays.asList(1, 2, 3));

        assertEquals("UPDATE t_dynamic_f0rb_i18n SET user_score = ? WHERE id IN (?, ?, ?)", sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly(100, 1, 2, 3);
    }

    @Test
    void buildDeleteAndArgsForDynamicTable() {
        DynamicQuery dynamicQuery = DynamicQuery
                .builder().user("f0rb").project("i18n").scoreLt(100)
                .pageNumber(3).pageSize(10).build();

        String expected = "DELETE FROM t_dynamic_f0rb_i18n " +
                "WHERE id IN (SELECT id FROM t_dynamic_f0rb_i18n t WHERE score < ? LIMIT 10 OFFSET 20)";
        SqlAndArgs sqlAndArgs = sqlBuilder.buildDeleteAndArgs(dynamicQuery);
        assertEquals(expected, sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly(100);
    }

    @Test
    void buildPatchAndArgs() {
        DynamicEntity entity = new DynamicEntity();
        entity.setId(1);
        entity.setUser("f0rb");
        entity.setProject("i18n");
        entity.setScore(100);

        DynamicQuery dynamicQuery = DynamicQuery
                .builder().user("f0rb").project("i18n").scoreLt(90)
                .pageNumber(3).pageSize(10).build();

        SqlAndArgs sqlAndArgs = sqlBuilder.buildPatchAndArgs(entity, dynamicQuery);

        String expected = "UPDATE t_dynamic_f0rb_i18n SET user_score = ? " +
                "WHERE id IN (SELECT id FROM t_dynamic_f0rb_i18n t WHERE score < ? LIMIT 10 OFFSET 20)";
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(100, 90);
    }

    @Test
    void buildUpdateAndArgsForCompositeId() {
        SqlBuilder<PartsuppEntity> sqlBuilder = new CrudBuilder<>(PartsuppEntity.class);

        PartsuppEntity entity = new PartsuppEntity();
        entity.setPs_partkey(1);
        entity.setPs_suppkey(2);
        entity.setPs_availqty(1000);
        entity.setPs_supplycost(BigDecimal.valueOf(20));

        SqlAndArgs sqlAndArgs = sqlBuilder.buildUpdateAndArgs(entity);

        String expected = "UPDATE t_partsupp SET ps_availqty = ?, ps_supplycost = ?, ps_comment = ? " +
                "WHERE ps_partkey = ? AND ps_suppkey = ?";
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(1000, BigDecimal.valueOf(20), null, 1, 2);
    }

    @Test
    void buildPatchAndArgsWithIdForCompositeId() {
        SqlBuilder<PartsuppEntity> sqlBuilder = new CrudBuilder<>(PartsuppEntity.class);

        PartsuppEntity entity = new PartsuppEntity();
        entity.setPs_partkey(1);
        entity.setPs_suppkey(2);
        entity.setPs_availqty(1000);

        SqlAndArgs sqlAndArgs = sqlBuilder.buildPatchAndArgsWithId(entity);

        String expected = "UPDATE t_partsupp SET ps_availqty = ? " +
                "WHERE ps_partkey = ? AND ps_suppkey = ?";
        assertThat(sqlAndArgs.getSql()).isEqualTo(expected);
        assertThat(sqlAndArgs.getArgs()).containsExactly(1000, 1, 2);
    }

    @Test
    void buildDeleteAndArgsForCompositeId() {
        SqlBuilder<PartsuppEntity> sqlBuilder = new CrudBuilder<>(PartsuppEntity.class);

        DynamicQuery dynamicQuery = DynamicQuery.builder().pageNumber(3).build();

        String expected = "DELETE FROM t_partsupp " +
                "WHERE (ps_partkey, ps_suppkey) IN " +
                "(SELECT ps_partkey, ps_suppkey FROM t_partsupp t " +
                "LIMIT 10 OFFSET 20)";
        SqlAndArgs sqlAndArgs = sqlBuilder.buildDeleteAndArgs(dynamicQuery);
        assertEquals(expected, sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).isEmpty();
    }
}