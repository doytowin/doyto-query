package win.doyto.query.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.core.module.user.UserEntity;
import win.doyto.query.core.module.user.UserQuery;

import java.util.ArrayList;
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
    private CrudBuilder<UserEntity> userEntityCrudBuilder = new CrudBuilder<>(UserEntity.class);
    private CrudBuilder<DynamicEntity> dynamicEntityCrudBuilder = new CrudBuilder<>(DynamicEntity.class);

    @BeforeEach
    void setUp() {

        dynamicEntity = new DynamicEntity();
        dynamicEntity.setUser("f0rb");
        dynamicEntity.setProject("i18n");
        dynamicEntity.setScore(100);

        argList = new ArrayList<>();
    }

    @Test
    void create() {
        assertEquals("INSERT INTO user (username, password, mobile, email, nickname, userLevel, valid) VALUES (?, ?, ?, ?, ?, ?, ?)",
                     userEntityCrudBuilder.buildCreateAndArgs(new UserEntity(), argList));
    }

    @Test
    void update() {
        assertEquals("UPDATE user SET username = ?, password = ?, mobile = ?, email = ?, nickname = ?, userLevel = ?, valid = ? WHERE id = ?",
                     userEntityCrudBuilder.buildUpdateAndArgs(new UserEntity(), argList));
    }

    @Test
    void createDynamicEntity() {

        assertEquals("INSERT INTO t_dynamic_f0rb_i18n (user_score, memo) VALUES (?, ?)",
                     dynamicEntityCrudBuilder.buildCreateAndArgs(dynamicEntity, argList));
        assertThat(argList).containsExactly(100, null);
    }

    @Test
    void updateDynamicEntity() {
        dynamicEntity.setId(1);

        assertEquals("UPDATE t_dynamic_f0rb_i18n SET user_score = ?, memo = ? WHERE id = ?",
                     dynamicEntityCrudBuilder.buildUpdateAndArgs(dynamicEntity, argList));
        assertThat(argList).containsExactly(100, null, 1);
    }


    @Test
    void buildPatchAndArgs() {
        dynamicEntity.setId(1);
        dynamicEntity.setScore(null);
        dynamicEntity.setMemo("memo");

        assertEquals("UPDATE t_dynamic_f0rb_i18n SET memo = ? WHERE id = ?",
                     dynamicEntityCrudBuilder.buildPatchAndArgsWithId(dynamicEntity, argList));
        assertThat(argList).containsExactly("memo", 1);
    }

    @Test
    void replaceTableName() {

        DynamicEntity entity = new DynamicEntity();
        entity.setId(1);
        entity.setUser("f0rb");
        entity.setProject("i18n");

        assertEquals("t_dynamic_f0rb_i18n", CommonUtil.replaceTableName(entity, DynamicEntity.TABLE));
        assertEquals("user", CommonUtil.replaceTableName(new UserEntity(), UserEntity.TABLE));

    }

    @Test
    public void supportMapFieldToUnderscore() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(true);

        try {
            UserEntity userEntity = new UserEntity();
            userEntity.setId(1);
            userEntity.setUserLevel("vip");
            userEntity.setValid(true);
            assertEquals("UPDATE user SET user_level = ?, valid = ? WHERE id = ?",
                         userEntityCrudBuilder.buildPatchAndArgsWithId(userEntity, argList));
        } finally {
            GlobalConfiguration.instance().setMapCamelCaseToUnderscore(false);
        }
    }

    @Test
    public void buildPatchAndArgsWithQuery() {
        UserEntity userEntity = new UserEntity();
        userEntity.setNickname("测试");

        UserQuery userQuery = UserQuery.builder().username("test").build();

        assertEquals("UPDATE user SET nickname = ? WHERE username = ?",
                     userEntityCrudBuilder.buildPatchAndArgsWithQuery(userEntity, userQuery, argList));
        assertThat(argList).containsExactly("测试", "test");
    }
}