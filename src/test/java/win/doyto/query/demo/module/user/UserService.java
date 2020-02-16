package win.doyto.query.demo.module.user;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import win.doyto.query.service.AbstractCrudService;

/**
 * UserService
 *
 * @author f0rb on 2020-01-01
 */
@Service
public class UserService extends AbstractCrudService<UserEntity, Long, UserQuery> {

    @Override
    protected String getCacheName() {
        return "module:user";
    }

    @Override
    protected RowMapper<UserEntity> getRowMapper() {
        return (rs, rn) -> {
            UserEntity userEntity = new UserEntity();
            userEntity.setId(rs.getLong("id"));
            userEntity.setUsername(rs.getString("username"));
            userEntity.setPassword(rs.getString("password"));
            userEntity.setNickname(rs.getString("nickname"));
            userEntity.setMobile(rs.getString("mobile"));
            userEntity.setEmail(rs.getString("email"));
            userEntity.setMemo(rs.getString("memo"));
            userEntity.setUserLevel(UserLevel.valueOf(rs.getString("userLevel")));
            userEntity.setValid(rs.getBoolean("valid"));
            return userEntity;
        };
    }
}
