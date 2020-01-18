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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CacheWrapperTest
 *
 * @author f0rb
 */
@SuppressWarnings("java:S2925")
public class CacheWrapperTest {

    private static class TestInvocable implements Invocable<String> {
        @Override
        public String invoke() {
            return "world";
        }
    }

    @Test
    public void invoke() throws InterruptedException {
        ConcurrentMapCache cache = new ConcurrentMapCache("test");
        String key = "hello";
        assertNull(cache.get(key));
        assertEquals("world", DefaultCacheWrapper.invoke(cache, key, new TestInvocable()));

        Thread.sleep(5L);

        assertNotNull(cache.get(key));
        assertEquals("world", DefaultCacheWrapper.invoke(cache, key, new TestInvocable()));
    }

    @Test
    public void whenCacheIsNoOpCache() {
        assertEquals(Integer.valueOf(1), DefaultCacheWrapper.invoke(new NoOpCache("noop"), "key", () -> 1));
    }

    @Test
    public void whenKeyIsNull() {
        ConcurrentMapCache cache = new ConcurrentMapCache("test");
        assertEquals(Integer.valueOf(1), DefaultCacheWrapper.invoke(cache, null, () -> 1));
    }

    @Test
    public void whenValueIsNull() throws InterruptedException {
        CacheWrapper<Object> cacheWrapper = CacheWrapper.createInstance();
        cacheWrapper.setCache(new ConcurrentMapCache("test"));

        @SuppressWarnings("unchecked")
        Invocable<Object> invocable = mock(Invocable.class);
        when(invocable.invoke()).thenReturn(null);
        assertNull(cacheWrapper.execute("t", invocable));
        assertNull(cacheWrapper.execute("t2", invocable));

        Thread.sleep(5L);

        assertNull(cacheWrapper.execute("t", invocable));
        assertNull(cacheWrapper.execute("t2", invocable));
        verify(invocable, times(2)).invoke();
    }

    @Test
    public void whenThrowCacheException() throws InterruptedException {
        CacheWrapper<Object> cacheWrapper = CacheWrapper.createInstance();
        cacheWrapper.setCache(new ConcurrentMapCache("test") {

            @Override
            public ValueWrapper get(Object key) {
                throw new RuntimeException("Timeout");
            }

            @Override
            public void put(Object key, Object value) {
                throw new RuntimeException("Timeout");
            }

        });


        AtomicInteger times = new AtomicInteger();
        Invocable<Object> invocable = times::incrementAndGet;
        assertEquals(1, cacheWrapper.execute("hello", invocable));

        Thread.sleep(5L);

        assertEquals(2, cacheWrapper.execute("hello", invocable));
    }

    @Test
    public void checkLogForPutException() throws InterruptedException {
        GlobalConfiguration.instance().setIgnoreCacheException(false);

        //given
        @SuppressWarnings("unchecked")
        Appender<ILoggingEvent> appender = mock(Appender.class);
        ((Logger) LoggerFactory.getLogger(DefaultCacheWrapper.class)).addAppender(appender);

        CacheWrapper<Object> cacheWrapper = CacheWrapper.createInstance();
        cacheWrapper.setCache(new ConcurrentMapCache("checkLog") {
            @Override
            public ValueWrapper get(Object key) {
                throw new RuntimeException("Timeout");
            }

            @Override
            public void put(Object key, Object value) {
                throw new RuntimeException("Timeout");
            }
        });

        //when
        AtomicInteger times = new AtomicInteger();
        Invocable<Object> invocable = times::incrementAndGet;
        assertEquals(1, cacheWrapper.execute("hello", invocable));
        Thread.sleep(5L);

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
