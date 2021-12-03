package win.doyto.query.cache;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * CacheProxyTest
 *
 * @author f0rb on 2021-02-26
 */
class CacheProxyTest {

    @Test
    void testZeroArgMethod() {

        //given
        @SuppressWarnings("unchecked")
        Appender<ILoggingEvent> appender = mock(Appender.class);
        ((Logger) LoggerFactory.getLogger(CacheProxy.class)).addAppender(appender);

        Cache cache = CacheProxy.wrap(new ConcurrentMapCache("test") {
            @Override
            public void clear() {
                throw new RuntimeException("test clear");
            }

            @Override
            public ValueWrapper get(Object key) {
                throw new RuntimeException("test get");
            }

            @Override
            public void put(Object key, Object value) {
                throw new RuntimeException("test put");
            }
        });

        //when
        cache.clear();
        cache.get("t");
        cache.put("k", "v");

        //then
        //通过ArgumentCaptor捕获所有log
        ArgumentCaptor<ILoggingEvent> logCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
        verify(appender, times(3)).doAppend(logCaptor.capture());
        assertThat(logCaptor.getAllValues())
                .hasSize(3)
                .extracting(ILoggingEvent::getFormattedMessage)
                .containsExactly(
                        "win.doyto.query.cache.CacheProxyTest$1#clear[cache=test, args=null] failed: test clear",
                        "win.doyto.query.cache.CacheProxyTest$1#get[cache=test, args=[t]] failed: test get",
                        "win.doyto.query.cache.CacheProxyTest$1#put[cache=test, args=[k, v]] failed: test put"
                );
    }
}