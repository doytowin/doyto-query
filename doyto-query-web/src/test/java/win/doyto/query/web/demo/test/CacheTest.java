package win.doyto.query.web.demo.test;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.springframework.cache.support.NoOpCache;
import win.doyto.query.cache.CacheWrapper;
import win.doyto.query.web.demo.module.role.RoleController;

import javax.annotation.Resource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CacheTest
 *
 * @author f0rb on 2021-07-16
 */
class CacheTest extends DemoApplicationTest {

    @Resource
    RoleController roleController;

    @Test
    void defaultNoCache() throws IllegalAccessException {
        Object service = FieldUtils.readField(roleController, "service", true);
        CacheWrapper<?> entityCacheWrapper = (CacheWrapper<?>) FieldUtils.readField(service, "entityCacheWrapper", true);
        assertThat(entityCacheWrapper.getCache()).isInstanceOf(NoOpCache.class);
    }
}
