package win.doyto.query.mybatis;

import org.apache.ibatis.annotations.Mapper;
import win.doyto.query.test.user.UserEntity;
import win.doyto.query.test.user.UserQuery;

/**
 * UserMapper
 *
 * @author f0rb
 */
@Mapper
@MapperTable(UserEntity.TABLE)
public interface UserMapper extends CrudMapper<UserEntity, Integer, UserQuery> {

}
