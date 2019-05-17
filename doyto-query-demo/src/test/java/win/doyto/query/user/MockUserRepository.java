package win.doyto.query.user;

import org.apache.commons.lang3.StringUtils;
import win.doyto.query.jpa2.AbstractMockRepository;

/**
 * MockUserRepository
 *
 * @author f0rb
 * @date 2019-05-15
 */
public class MockUserRepository extends AbstractMockRepository<UserEntity, Long, UserQuery> implements UserRepository {
    public MockUserRepository() {
        super(UserEntity.TABLE);
    }

    @Override
    protected boolean unsatisfied(UserEntity entity, String fieldName, Object v1) {
        if (fieldName.equals("usernameOrEmailOrMobile")) {
            String account = v1.toString();
            if (!StringUtils.equals(entity.getUsername(), account) &&
                !StringUtils.equals(entity.getEmail(), account) &&
                !StringUtils.equals(entity.getMobile(), account)
            ) {
                return true;
            }
        }
        return super.unsatisfied(entity, fieldName, v1);
    }
}
