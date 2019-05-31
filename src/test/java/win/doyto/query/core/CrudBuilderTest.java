package win.doyto.query.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.user.UserEntity;

import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CrudBuilderTest
 *
 * @author f0rb
 */
class CrudBuilderTest {

    private DynamicEntity dynamicEntity;
    private LinkedList<Object> argList;
    private CrudBuilder<UserEntity> userEntityCrudBuilder = new CrudBuilder<>(UserEntity.class);
    private CrudBuilder<DynamicEntity> dynamicEntityCrudBuilder = new CrudBuilder<>(DynamicEntity.class);

    @BeforeEach
    void setUp() {

        dynamicEntity = new DynamicEntity();
        dynamicEntity.setUser("f0rb");
        dynamicEntity.setProject("i18n");
        dynamicEntity.setScore(100);

        argList = new LinkedList<>();
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
                     dynamicEntityCrudBuilder.buildPatchAndArgs(dynamicEntity, argList));
        assertThat(argList).containsExactly("memo", 1);
    }

    @Test
    void replaceTableName() {

        DynamicEntity entity = new DynamicEntity();
        entity.setId(1);
        entity.setUser("f0rb");
        entity.setProject("i18n");

        assertEquals("t_dynamic_f0rb_i18n", CrudBuilder.replaceTableName(entity, DynamicEntity.TABLE));
        assertEquals("user", CrudBuilder.replaceTableName(new UserEntity(), UserEntity.TABLE));

    }

    @Test
    public void supportMapFieldToUnderscore() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(true);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1);
        userEntity.setUserLevel("vip");
        assertEquals("UPDATE user SET user_level = ?, valid = ? WHERE id = ?",
                     userEntityCrudBuilder.buildPatchAndArgs(userEntity, argList));

        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(false);
    }
}