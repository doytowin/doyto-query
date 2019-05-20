package win.doyto.query.module.user;

import org.springframework.data.repository.CrudRepository;

/**
 * UserRepository
 *
 * @author f0rb
 */
interface UserRepository extends CrudRepository<UserEntity, Long> {
}
