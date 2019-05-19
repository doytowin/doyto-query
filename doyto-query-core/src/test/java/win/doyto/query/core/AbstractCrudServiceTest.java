package win.doyto.query.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import win.doyto.query.user.MockUserDataAccess;
import win.doyto.query.user.UserEntity;
import win.doyto.query.user.UserService;

import static org.mockito.Mockito.*;
import static win.doyto.query.core.AbstractMockDataAccessTest.initUserEntities;

/**
 * AbstractCrudServiceTest
 *
 * @author f0rb
 * @date 2019-05-18
 */
class AbstractCrudServiceTest {
    UserService userService;
    MockUserDataAccess dataAccess;

    @BeforeEach
    void setUp() {
        dataAccess = spy(new MockUserDataAccess());
        userService = new UserService(dataAccess);
        userService.save(initUserEntities());
    }

    @Test
    void supportCache() {
        userService.setCacheManager(new ConcurrentMapCacheManager());
        userService.get(1);
        userService.get(1);
        verify(dataAccess, times(1)).get(1);
    }

    @Test
    void supportEvictCache() {
        userService.setCacheManager(new ConcurrentMapCacheManager());
        UserEntity userEntity = userService.get(1);
        userService.save(userEntity);
        userService.get(1);
        verify(dataAccess, times(2)).get(1);
    }
}