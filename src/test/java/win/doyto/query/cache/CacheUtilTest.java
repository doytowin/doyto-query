package win.doyto.query.cache;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.NoOpCache;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.Invocable;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

/**
 * CacheUtilTest
 *
 * @author f0rb on 2020-05-16
 */
class CacheUtilTest {
    private static class TestInvocable implements Invocable<String> {
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
        assertEquals("world", CacheUtil.invoke(cache, key, new TestInvocable()));

        while(cache.get(key) == null) {
            System.out.println("waiting...");
        }
        assertEquals("world", CacheUtil.invoke(cache, key, new TestInvocable()));
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
        Invocable<Object> invocable = times::incrementAndGet;
        assertEquals(1, cacheWrapper.execute("hello", invocable));

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