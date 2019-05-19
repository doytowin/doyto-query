package win.doyto.query.jpa2;

import org.springframework.data.repository.CrudRepository;
import win.doyto.query.test.user.UserEntity;

/**
 * UserRepository
 *
 * @author f0rb
 * @date 2019-05-19
 */
public interface UserRepository extends CrudRepository<UserEntity, Integer> {
}
