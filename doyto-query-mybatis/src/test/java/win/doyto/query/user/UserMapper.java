package win.doyto.query.user;

import org.apache.ibatis.annotations.Mapper;
import win.doyto.query.mybatis.QueryMapper;

/**
 * UserMapper
 *
 * @author f0rb
 * @date 2019-05-12
 */
@Mapper
public interface UserMapper extends QueryMapper<UserEntity, Integer, UserQuery> {

}
