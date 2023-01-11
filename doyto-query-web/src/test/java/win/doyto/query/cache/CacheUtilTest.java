/*
 * Copyright © 2019-2023 Forb Yuan
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

package win.doyto.query.cache;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.NoOpCache;
import win.doyto.query.config.GlobalConfiguration;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.parallel.ResourceAccessMode.READ_WRITE;
import static org.mockito.Mockito.*;

/**
 * CacheUtilTest
 *
 * @author f0rb on 2020-05-16
 */
class CacheUtilTest {
    private static class TestCacheInvoker implements CacheInvoker<String> {
        @Override
        public String invoke() {
            return "world";
        }
    }

    @Test
    void invoke() {
        ConcurrentMapCache cache = new ConcurrentMapCache("test");
        String key = "hello";
        assertNull(cache.get(key));
        assertEquals("world", CacheUtil.invoke(cache, key, new TestCacheInvoker()));

        while(cache.get(key) == null) {
            System.out.println("waiting...");
        }
        assertEquals("world", CacheUtil.invoke(cache, key, new TestCacheInvoker()));
    }

    @Test
    void whenCacheIsNoOpCache() {
        assertEquals(Integer.valueOf(1), CacheUtil.invoke(new NoOpCache("noop"), "key", () -> 1));
    }

    @Test
    void whenKeyIsNull() {
        ConcurrentMapCache cache = new ConcurrentMapCache("test");
        assertEquals(Integer.valueOf(1), CacheUtil.invoke(cache, null, () -> 1));
    }

    @ResourceLock(value = "ignoreCacheException", mode = READ_WRITE)
    @Test
    void checkLogForPutException() {
        GlobalConfiguration.instance().setIgnoreCacheException(false);

        //given
        @SuppressWarnings("unchecked")
        Appender<ILoggingEvent> appender = mock(Appender.class);
        ((Logger) LoggerFactory.getLogger(CacheUtil.class)).addAppender(appender);

        CacheWrapper<Object> cacheWrapper = CacheWrapper.createInstance();
        cacheWrapper.setCache(new ConcurrentMapCache("checkLog") {
            @Override
            public ValueWrapper get(Object key) {
                throw new RuntimeException("get timeout");
            }

            @Override
            public void put(Object key, Object value) {
                throw new RuntimeException("put timeout");
            }
        });

        //when
        AtomicInteger times = new AtomicInteger();
        CacheInvoker<Object> cacheInvoker = times::incrementAndGet;
        assertEquals(1, cacheWrapper.execute("hello", cacheInvoker));

        //then
        //通过ArgumentCaptor捕获所有log
        ArgumentCaptor<ILoggingEvent> logCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
        verify(appender, times(1)).doAppend(logCaptor.capture());
        assertThat(logCaptor.getAllValues())
                .hasSize(1)
                .extracting(ILoggingEvent::getMessage)
                .containsExactly(
                        "Cache#get failed: [cache=checkLog, key=hello]"
                );

        GlobalConfiguration.instance().setIgnoreCacheException(true);

    }
}