package win.doyto.query.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import win.doyto.query.entity.EntityAspect;
import win.doyto.query.user.UserEntity;
import win.doyto.query.user.UserQuery;
import win.doyto.query.user.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static win.doyto.query.user.UserEntity.initUserEntities;

/**
 * AbstractCrudServiceTest
 *
 * @author f0rb
 */
class AbstractCrudServiceTest {
    UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        userService.dataAccess = spy(userService.dataAccess);
        userService.batchInsert(initUserEntities());
    }

    @Test
    void supportCache() {
        userService.setCacheManager(new ConcurrentMapCacheManager());
        userService.get(1);
        userService.get(1);
        verify(userService.dataAccess, times(1)).get(1);
    }

    @Test
    void supportEvictCache() {
        userService.setCacheManager(new ConcurrentMapCacheManager());
        UserEntity userEntity = userService.get(1);
        userService.update(userEntity);
        userService.get(1);
        verify(userService.dataAccess, times(2)).get(1);
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

    @Test
    void count() {
        assertEquals(1, userService.count(UserQuery.builder().username("username1").build()));
    }

    @Test
    void exists() {
        assertTrue(userService.exists(UserQuery.builder().username("username1").build()));
    }

    @Test
    void deleteByQuery() {
        UserQuery userQuery = UserQuery.builder().usernameLike("username").build();
        assertEquals(4, userService.delete(userQuery));
    }

    @Test
    void deleteByQueryWithLimit() {
        UserQuery userQuery = UserQuery.builder().usernameLike("username").build();
        userQuery.setPageSize(2);
        assertEquals(2, userService.delete(userQuery));
    }
}