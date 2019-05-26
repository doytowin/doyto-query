package win.doyto.query.core;

import org.junit.jupiter.api.Test;
import win.doyto.query.user.UserEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * CrudBuilderTest
 *
 * @author f0rb
 */
class CrudBuilderTest {

    @Test
    void create() {
        assertEquals("INSERT INTO user (username, password, mobile, email, nickname, valid) VALUES (#{username}, #{password}, #{mobile}, #{email}, #{nickname}, #{valid})",
                     new CrudBuilder().create(new UserEntity()));
    }

    @Test
    void update() {
        assertEquals("UPDATE user SET username = #{username}, password = #{password}, mobile = #{mobile}, email = #{email}, nickname = #{nickname}, valid = #{valid} WHERE id = #{id}",
                     new CrudBuilder().update(new UserEntity()));
    }
}