package win.doyto.query.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import win.doyto.query.entity.EntityAspect;
import win.doyto.query.user.MockUserDataAccess;
import win.doyto.query.user.UserEntity;
import win.doyto.query.user.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.*;
import static win.doyto.query.core.AbstractMockDataAccessTest.initUserEntities;

/**
 * AbstractCrudServiceTest
 *
 * @author f0rb
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

    @Test
    void supportAspect() {
        EntityAspect<UserEntity> entityAspect = spy(new EntityAspect<UserEntity>() {
            @Override
            public void afterUpdate(UserEntity origin, UserEntity current) {
                assertNotSame(origin, current);
                assertEquals("test1", origin.getUsername());
                assertEquals("test2", current.getUsername());
            }
        });

        userService.entityAspects.add(entityAspect);
        UserEntity e = new UserEntity();
        e.setUsername("test1");
        userService.create(e);
        verify(entityAspect, times(1)).afterCreate(e);

        UserEntity u = new UserEntity();
        u.setId(e.getId());
        u.setUsername("test2");
        userService.update(u);
        verify(entityAspect, times(1)).afterUpdate(any(), eq(u));

        userService.delete(e.getId());
        verify(entityAspect, times(1)).afterDelete(u);
    }
}