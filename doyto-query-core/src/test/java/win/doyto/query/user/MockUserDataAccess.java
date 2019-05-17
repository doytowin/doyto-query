package win.doyto.query.user;

import win.doyto.query.core.AbstractMockDataAccess;

/**
 * MockUserDataAccess
 *
 * @author f0rb
 * @date 2019-05-17
 */
public class MockUserDataAccess extends AbstractMockDataAccess<UserEntity, Integer, UserQuery> {
    public MockUserDataAccess() {
        super(UserEntity.TABLE);
    }
}
