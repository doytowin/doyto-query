package win.doyto.query.service;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import win.doyto.query.core.test.TestEntity;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

/**
 * CollectionUtilTest
 *
 * @author f0rb
 */
class CollectionUtilTest {

    @Test
    void first() {
        assertNull(CollectionUtil.first(Arrays.asList()));
        assertEquals("hello", CollectionUtil.first(Arrays.asList("hello")));
        assertEquals("hello", CollectionUtil.first(Arrays.asList("hello", "world")));
    }

    @Test
    void repetitiveWithLog() {
        //given
        @SuppressWarnings("unchecked")
        ch.qos.logback.core.Appender<ch.qos.logback.classic.spi.ILoggingEvent> appender = mock(ch.qos.logback.core.Appender.class);
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(CollectionUtil.class)).addAppender(appender);

        //when
        TestEntity testEntity = new TestEntity();
        testEntity.setUsername("test");
        testEntity.setPassword("password");
        assertNull(CollectionUtil.first(Arrays.asList(new TestEntity(), testEntity)).getUsername());

        //then
        ArgumentCaptor<ILoggingEvent> logCaptor = ArgumentCaptor.forClass(ch.qos.logback.classic.spi.ILoggingEvent.class);
        //通过ArgumentCaptor捕获所有log
        verify(appender, times(2)).doAppend(logCaptor.capture());
        logCaptor.getAllValues().stream()
                 .filter(event -> {
                     String formattedMessage = event.getFormattedMessage();
                     return formattedMessage.equals("Repetitive elements: \nwin.doyto.query.core.test.TestEntity[]\nwin.doyto.query.core.test.TestEntity[username=test,password=password]");
                 })
                 .findFirst()
                 .orElseThrow(AssertionError::new);
    }
}