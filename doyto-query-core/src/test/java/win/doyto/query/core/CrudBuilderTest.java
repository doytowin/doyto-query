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
        assertEquals("INSERT INTO user (username, password, mobile, email, nickname, valid) VALUES (#{username}, #{password}, #{mobile}, #{email}, #{nickname}, #{valid})",
                     new CrudBuilder<>(UserEntity.class).buildCreate(new UserEntity()));
    }

    @Test
    void update() {
        assertEquals("UPDATE user SET username = #{username}, password = #{password}, mobile = #{mobile}, email = #{email}, nickname = #{nickname}, valid = #{valid} WHERE id = #{id}",
                     new CrudBuilder<>(UserEntity.class).buildUpdate(new UserEntity()));
    }

    @Test
    void createDynamicEntity() {
        assertEquals("INSERT INTO t_dynamic_f0rb_i18n (user_score, memo) VALUES (#{score}, #{memo})",
                     new CrudBuilder<>(DynamicEntity.class).buildCreate(dynamicEntity));

        assertEquals("INSERT INTO t_dynamic_f0rb_i18n (user_score, memo) VALUES (?, ?)",
                     new CrudBuilder<>(DynamicEntity.class).buildCreateAndArgs(dynamicEntity, argList));
        assertThat(argList).containsExactly(100, null);
    }

    @Test
    void updateDynamicEntity() {
        dynamicEntity.setId(1);
        assertEquals("UPDATE t_dynamic_f0rb_i18n SET user_score = #{score}, memo = #{memo} WHERE id = #{id}",
                     dynamicEntityCrudBuilder.buildUpdate(dynamicEntity));

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
}