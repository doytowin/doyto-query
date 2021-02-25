package win.doyto.query.cache;

import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import win.doyto.query.core.Invocable;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

/**
 * CacheWrapperTest
 *
 * @author f0rb
 */
@SuppressWarnings("java:S2925")
class CacheWrapperTest {

    @Test
    void whenValueIsNull() throws InterruptedException {
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
    void whenThrowCacheException() throws InterruptedException {
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

}
