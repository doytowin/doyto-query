package win.doyto.query.user;

import org.apache.ibatis.annotations.Mapper;
import win.doyto.query.mybatis.CrudMapper;
import win.doyto.query.mybatis.MapperTable;

/**
 * UserMapper
 *
 * @author f0rb
 * @date 2019-05-12
 */
@Mapper
@MapperTable(UserEntity.TABLE)
public interface UserMapper extends CrudMapper<UserEntity, Integer, UserQuery> {

}
