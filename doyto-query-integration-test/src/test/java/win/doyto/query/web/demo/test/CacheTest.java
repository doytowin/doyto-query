/*
 * Copyright © 2019-2022 Forb Yuan
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

package win.doyto.query.web.demo.test;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.springframework.cache.support.NoOpCache;
import win.doyto.query.cache.CacheWrapper;
import win.doyto.query.web.demo.module.role.RoleController;

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
