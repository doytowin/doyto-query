/*
 * Copyright © 2019-2024 Forb Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.doyto.query.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.core.PageList;
import win.doyto.query.entity.EntityAspect;
import win.doyto.query.test.TestEntity;
import win.doyto.query.test.TestQuery;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static win.doyto.query.test.TestEntity.initUserEntities;

/**
 * AbstractServiceTest
 *
 * @author f0rb
 */
class AbstractDynamicServiceTest {
    TestService testService;
    DataAccess<TestEntity, Integer, TestQuery> spyDataAccess;

    @BeforeEach
    void setUp() {
        testService = new TestService();
        testService.dataAccess = spy(testService.dataAccess);
        spyDataAccess = testService.dataAccess;
        testService.create(initUserEntities());
    }

    @Test
    @SuppressWarnings("java:S2925")
    void supportCache() throws InterruptedException {
        testService.setCacheManager(new ConcurrentMapCacheManager());
        testService.setCacheList("");
        testService.afterPropertiesSet();

        testService.get(1);

        Thread.sleep(10L);

        testService.get(1);
        verify(spyDataAccess, times(1)).get(IdWrapper.build(1));
    }

    @Test
    void supportEvictCache() {
        TestEntity testEntity = testService.get(1);
        testService.update(testEntity);
        testService.get(1);
        verify(spyDataAccess, times(2)).get(IdWrapper.build(1));
    }

    @Test
    void supportAspect() {
        EntityAspect<TestEntity> entityAspect = spy(new EntityAspect<TestEntity>() {
            @Override
            public void afterUpdate(TestEntity origin, TestEntity current) {
                assertNotSame(origin, current);
                assertEquals("test1", origin.getUsername());
                assertEquals("test2", current.getUsername());
            }
        });

        testService.entityAspects.add(entityAspect);
        testService.afterPropertiesSet();

        TestEntity e = new TestEntity();
        e.setUsername("test1");
        testService.create(e);
        verify(entityAspect, times(1)).afterCreate(e);

        TestEntity u = new TestEntity();
        u.setId(e.getId());
        u.setUsername("test2");
        testService.update(u);
        verify(entityAspect, times(1)).afterUpdate(any(), any());

        testService.remove(e.getId());
        verify(entityAspect, times(1)).afterDelete(any(TestEntity.class));
    }

    @Test
    void count() {
        assertEquals(1, testService.count(TestQuery.builder().username("username1").build()));
    }

    @Test
    void exists() {
        assertTrue(testService.exists(TestQuery.builder().username("username1").build()));
    }

    @Test
    void notExists() {
        assertFalse(testService.notExists(TestQuery.builder().username("username1").build()));
    }

    @Test
    void deleteByQuery() {
        TestQuery testQuery = TestQuery.builder().usernameLike("username").build();
        assertEquals(4, testService.delete(testQuery));
    }

    @Test
    void deleteByQueryWithLimit() {
        TestQuery testQuery = TestQuery.builder().usernameLike("username").build();
        testQuery.setPageSize(2);
        assertEquals(2, testService.delete(testQuery));
    }

    @Test
    void queryIds() {
        TestQuery testQuery = TestQuery.builder().usernameLike("f0rb").build();
        List<Integer> ids = testService.queryIds(testQuery);
        assertEquals(1, ids.size());
        assertEquals(5, (int) ids.get(0));
    }

    @Test
    void forcePage() {
        TestQuery testQuery = new TestQuery();
        PageList<TestEntity> pageList = testService.page(testQuery);
        assertEquals(5, pageList.getTotal());
        assertEquals(0, testQuery.getPageNumber());
        assertEquals(10, testQuery.getPageSize());
    }

    @Test
    void pageAndTransform() {
        TestQuery testQuery = new TestQuery();
        PageList<String> pageList = testService.page(testQuery, TestEntity::getUsername);
        assertThat(pageList.getList())
                .containsExactly("username1", "username2", "username3", "username4", "f0rb");
    }
}