package win.doyto.query.cache;

import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.NoOpCache;
import win.doyto.query.core.Invocable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CacheWrapperTest
 *
 * @author f0rb
 */
public class CacheWrapperTest {

    private static class TestInvocable implements Invocable<String> {
        @Override
        public String invoke() {
            return "world";
        }
    }

    @Test
    public void invoke() {
        ConcurrentMapCache cache = new ConcurrentMapCache("test");
        String key = "hello";
        assertNull(cache.get(key));
        assertEquals("world", CacheWrapper.invoke(cache, key, new TestInvocable()));
        assertNotNull(cache.get(key));
        assertEquals("world", CacheWrapper.invoke(cache, key, new TestInvocable()));
    }

    @Test
    public void whenCacheIsNoOpCache() {
        assertEquals(Integer.valueOf(1), CacheWrapper.invoke(new NoOpCache("noop"), "key", () -> 1));
    }

    @Test
    public void whenKeyIsNull() {
        ConcurrentMapCache cache = new ConcurrentMapCache("test");
        assertEquals(Integer.valueOf(1), CacheWrapper.invoke(cache, null, () -> 1));
    }

    @Test
    public void whenValueIsNull() {
        CacheWrapper<Object> cacheWrapper = CacheWrapper.createInstance();
        cacheWrapper.setCache(new ConcurrentMapCache("test"));

        @SuppressWarnings("unchecked")
        Invocable<Object> invocable = mock(Invocable.class);
        when(invocable.invoke()).thenReturn(null);
        assertNull(cacheWrapper.execute("t", invocable));
        assertNull(cacheWrapper.execute("t", invocable));
        assertNull(cacheWrapper.execute("t2", invocable));
        assertNull(cacheWrapper.execute("t2", invocable));
        verify(invocable, times(2)).invoke();
    }
}
