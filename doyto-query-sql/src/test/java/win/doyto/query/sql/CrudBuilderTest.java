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

package win.doyto.query.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.test.*;
import win.doyto.query.util.CommonUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CrudBuilderTest
 *
 * @author f0rb
 */
class CrudBuilderTest {

    private DynamicEntity dynamicEntity;
    private List<Object> argList;
    private CrudBuilder<TestEntity> testEntityCrudBuilder = new CrudBuilder<>(TestEntity.class);
    private CrudBuilder<DynamicEntity> dynamicEntityCrudBuilder = new CrudBuilder<>(DynamicEntity.class);

    @BeforeEach
    void setUp() {

        dynamicEntity = new DynamicEntity();
        dynamicEntity.setUser("f0rb");
        dynamicEntity.setProject("i18n");
        dynamicEntity.setLocale("zh");
        dynamicEntity.setValue("中文");
        dynamicEntity.setScore(100);

        argList = new ArrayList<>();
    }

    @Test
    void create() {
        SqlAndArgs sqlAndArgs = testEntityCrudBuilder.buildCreateAndArgs(new TestEntity());
        String expected = "INSERT INTO t_user (username, password, mobile, email, nickname, user_level, memo, score, valid, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        assertEquals(expected, sqlAndArgs.getSql());
    }

    @Test
    void update() {
        TestEntity testEntity = new TestEntity();
        testEntity.setId(2);
        testEntity.setVersion(20);

        SqlAndArgs sqlAndArgs = testEntityCrudBuilder.buildUpdateAndArgs(testEntity);
        String expected = "UPDATE t_user SET username = ?, password = ?, mobile = ?, email = ?, nickname = ?, " +
                "user_level = ?, memo = ?, score = ?, valid = ?, version = version + 1 WHERE id = ? AND version = ?";
        assertEquals(expected, sqlAndArgs.getSql());
    }

    @Test
    void createDynamicEntity() {

        SqlAndArgs sqlAndArgs = dynamicEntityCrudBuilder.buildCreateAndArgs(dynamicEntity);
        assertEquals("INSERT INTO t_dynamic_f0rb_i18n (locale_zh, user_score, memo) VALUES (?, ?, ?)", sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly("中文", 100, null);
    }

    @Test
    void updateDynamicEntity() {
        dynamicEntity.setId(1);

        SqlAndArgs sqlAndArgs = dynamicEntityCrudBuilder.buildUpdateAndArgs(dynamicEntity);
        assertEquals("UPDATE t_dynamic_f0rb_i18n SET locale_zh = ?, user_score = ?, memo = ? WHERE id = ?", sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly("中文", 100, null, 1);
    }


    @Test
    void buildPatchAndArgs() {
        dynamicEntity.setId(1);
        dynamicEntity.setValue(null);
        dynamicEntity.setScore(null);
        dynamicEntity.setMemo("memo");

        SqlAndArgs sqlAndArgs = dynamicEntityCrudBuilder.buildPatchAndArgsWithId(dynamicEntity);

        assertEquals("UPDATE t_dynamic_f0rb_i18n SET memo = ? WHERE id = ?", sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly("memo", 1);
    }

    @Test
    void replaceTableName() {

        DynamicEntity entity = new DynamicEntity();
        entity.setId(1);
        entity.setUser("f0rb");
        entity.setProject("i18n");

        assertEquals("t_dynamic_f0rb_i18n", CommonUtil.replaceHolderInString(entity, DynamicEntity.TABLE));
        assertEquals("t_user", CommonUtil.replaceHolderInString(new TestEntity(), TestEntity.TABLE));

    }

    @Test
    void fixReplaceTableNameWithTail() {

        DynamicEntity entity = new DynamicEntity();
        entity.setId(1);
        entity.setUser("f0rb");
        entity.setProject("i18n");

        assertEquals("t_dynamic_f0rb_i18n_any", CommonUtil.replaceHolderInString(entity, "t_dynamic_${user}_${project}" + "_any"));

    }

    @Test
    void supportMapFieldToUnderscore() {
        TestEntity testEntity = new TestEntity();
        testEntity.setId(1);
        testEntity.setUserLevel(TestEnum.VIP);
        testEntity.setValid(true);

        SqlAndArgs sqlAndArgs = testEntityCrudBuilder.buildPatchAndArgsWithId(testEntity);

        assertEquals("UPDATE t_user SET version = version + 1, user_level = ?, valid = ? WHERE id = ?", sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly(0, true, 1);
    }

    @Test
    void createMulti() {
        SqlAndArgs sqlAndArgs = testEntityCrudBuilder.buildCreateAndArgs(Arrays.asList(new TestEntity(), new TestEntity(), new TestEntity()));
        assertEquals(
                "INSERT INTO t_user (username, password, mobile, email, nickname, user_level, memo, score, valid, version) VALUES " +
                        "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?), (?, ?, ?, ?, ?, ?, ?, ?, ?, ?), (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", sqlAndArgs.getSql());
    }

    @Test
    void supportDynamicTableName() {
        DynamicQuery dynamicQuery = DynamicQuery.builder().user("f0rb").project("i18n").scoreLt(100).build();

        assertEquals("SELECT * FROM t_dynamic_f0rb_i18n t WHERE score < ?",
                     dynamicEntityCrudBuilder.buildSelectAndArgs(dynamicQuery, argList));
        assertThat(argList).containsExactly(100);
    }

    @Test
    void fixSQLInject() {
        DynamicQuery dynamicQuery = DynamicQuery.builder().user("f0rb").project("; DROP TABLE menu;").scoreLt(80).build();

        assertEquals("SELECT * FROM t_dynamic_f0rb_${project} t WHERE score < ?",
                     dynamicEntityCrudBuilder.buildSelectAndArgs(dynamicQuery, argList));
        assertThat(argList).containsExactly(80);
    }

    @Test
    void supportUnderlineScore() {
        DynamicQuery dynamicQuery = DynamicQuery.builder().user("f0rb").project("i18n_0001").scoreLt(100).build();

        assertEquals("SELECT * FROM t_dynamic_f0rb_i18n_0001 t WHERE score < ?",
                     dynamicEntityCrudBuilder.buildSelectAndArgs(dynamicQuery, argList));
        assertThat(argList).containsExactly(100);
    }

    @Test
    void createMultiOnDuplicate() {
        SqlAndArgs sqlAndArgs = testEntityCrudBuilder.buildCreateAndArgs(
                Arrays.asList(new TestEntity(), new TestEntity(), new TestEntity()),
                "mobile", "email"
        );
        assertEquals(
                "INSERT INTO t_user (username, password, mobile, email, nickname, user_level, memo, score, valid, version) VALUES " +
                        "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?), (?, ?, ?, ?, ?, ?, ?, ?, ?, ?), (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" +
                        " ON DUPLICATE KEY UPDATE " +
                        "mobile = VALUES (mobile), " +
                        "email = VALUES (email)",
                sqlAndArgs.getSql());
    }

    @Test
    void buildPatchAndArgsForDynamicColumn() {
        dynamicEntity.setId(1);
        SqlAndArgs sqlAndArgs = dynamicEntityCrudBuilder.buildPatchAndArgsWithId(dynamicEntity);
        assertEquals("UPDATE t_dynamic_f0rb_i18n SET locale_zh = ?, user_score = ? WHERE id = ?", sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly("中文", 100, 1);
    }

    @Test
    void supportCompoundOperators() {
        TestEntity testPatch = TestPatch.builder().id(1).valid(true).scoreAe(20).build();

        SqlAndArgs sqlAndArgs = testEntityCrudBuilder.buildPatchAndArgsWithId(testPatch);

        assertEquals("UPDATE t_user SET version = version + 1, valid = ?, score = score + ? WHERE id = ?", sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly(true, 20, 1);
    }

    @Test
    void supportCustomSetClause() {
        TestEntity testPatch = TestPatch.builder().id(1).valid(true).scoreDec(20).build();

        SqlAndArgs sqlAndArgs = testEntityCrudBuilder.buildPatchAndArgsWithId(testPatch);

        assertEquals("UPDATE t_user SET version = version + 1, valid = ?, score = score - ? WHERE id = ?", sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly(true, 20, 1);
    }

    @Test
    void supportUpdateWith() {
        TestEntity testEntity = TestEntity.builder().valid(true).id(5).version(21).build();

        SqlAndArgs sqlAndArgs = testEntityCrudBuilder.buildPatchAndArgsWithId(testEntity);

        assertEquals("UPDATE t_user SET version = version + 1, valid = ? WHERE id = ? AND version = ?", sqlAndArgs.getSql());
        assertThat(sqlAndArgs.getArgs()).containsExactly(true, 5, 21);
    }

}