package win.doyto.query.user;

import org.springframework.data.repository.CrudRepository;

/**
 * UserRepository
 *
 * @author f0rb
 * @date 2019-05-12
 */
public interface UserRepository extends CrudRepository<UserEntity, Integer> {
}
