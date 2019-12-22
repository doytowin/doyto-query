package win.doyto.query.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import win.doyto.query.core.test.TestEntity;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
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
        Appender<ILoggingEvent> appender = mock(Appender.class);
        ((Logger) LoggerFactory.getLogger(CollectionUtil.class)).addAppender(appender);

        //when
        TestEntity testEntity = new TestEntity();
        testEntity.setUsername("test");
        testEntity.setPassword("password");
        assertNull(CollectionUtil.first(Arrays.asList(new TestEntity(), testEntity)).getUsername());

        //then
        //通过ArgumentCaptor捕获所有log
        ArgumentCaptor<ILoggingEvent> logCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
        verify(appender, times(3)).doAppend(logCaptor.capture());

        assertThat(logCaptor.getAllValues())
            .hasSize(3)
            .extracting(ILoggingEvent::getFormattedMessage)
            .contains(
                "Find 2 elements of class win.doyto.query.core.test.TestEntity",
                "Repetitive elements: \nwin.doyto.query.core.test.TestEntity[]\nwin.doyto.query.core.test.TestEntity[username=test,password=password]"
            );

    }

    @Test
    void logThreeElementsAtMost() {
        //given
        @SuppressWarnings("unchecked")
        Appender<ILoggingEvent> appender = mock(Appender.class);
        ((Logger) LoggerFactory.getLogger(CollectionUtil.class)).addAppender(appender);

        //when
        assertEquals("hello", CollectionUtil.first(Arrays.asList("hello", "three", "logs", "at most")));

        //then
        //通过ArgumentCaptor捕获所有log
        ArgumentCaptor<ILoggingEvent> logCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
        verify(appender, times(3)).doAppend(logCaptor.capture());

        assertThat(logCaptor.getAllValues())
                .hasSize(3)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains(
                        "Find 4 elements of class java.lang.String",
                        "Repetitive elements: \nhello\nthree\nlogs\n..."
                );
    }
}