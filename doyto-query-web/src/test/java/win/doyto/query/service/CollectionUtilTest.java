package win.doyto.query.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import win.doyto.query.test.TestEntity;
import win.doyto.query.test.TestQuery;

import java.util.Arrays;
import java.util.List;

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

    private static final int LOG_COUNT = 3;

    @Test
    void first() {
        @SuppressWarnings("unchecked")
        Appender<ILoggingEvent> appender = mock(Appender.class);
        ((Logger) LoggerFactory.getLogger(CollectionUtil.class)).addAppender(appender);
        assertNull(CollectionUtil.first(Arrays.asList()));
        assertEquals("hello", CollectionUtil.first(Arrays.asList("hello")));
        assertEquals("hello", CollectionUtil.first(Arrays.asList("hello", "world")));
        ArgumentCaptor<ILoggingEvent> logCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
        verify(appender, times(LOG_COUNT)).doAppend(logCaptor.capture());
    }

    @Test
    void repetitiveWithLog() {
        //given
        @SuppressWarnings("unchecked")
        Appender<ILoggingEvent> appender = mock(Appender.class);
        ((Logger) LoggerFactory.getLogger(CollectionUtil.class)).addAppender(appender);

        TestEntity testEntity = new TestEntity();
        testEntity.setUsername("test1");
        testEntity.setPassword("password");

        TestEntity testEntity2 = new TestEntity();
        testEntity2.setUsername("test2");

        List<TestEntity> queryResult = Arrays.asList(testEntity2, testEntity);
        TestQuery query = TestQuery.builder().usernameLike("test").build();

        //when
        CollectionUtil.debugRepetitiveElements(queryResult, query);

        //then
        //通过ArgumentCaptor捕获所有log
        ArgumentCaptor<ILoggingEvent> logCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
        verify(appender, times(LOG_COUNT)).doAppend(logCaptor.capture());

        assertThat(logCaptor.getAllValues())
                .hasSize(LOG_COUNT)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains(
                        "Found 2 elements of class win.doyto.query.test.TestEntity for query: " +
                                "win.doyto.query.test.TestQuery[usernameLike=test,memoNull=false,memoNotNull=false]",
                        "Repetitive elements: \nwin.doyto.query.test.TestEntity[username=test2]\n" +
                                "win.doyto.query.test.TestEntity[username=test1,password=password]"
                );

    }

    @Test
    void logThreeElementsAtMost() {
        //given
        @SuppressWarnings("unchecked")
        Appender<ILoggingEvent> appender = mock(Appender.class);
        ((Logger) LoggerFactory.getLogger(CollectionUtil.class)).addAppender(appender);

        //when
        CollectionUtil.debugRepetitiveElements(Arrays.asList("hello", "three", "logs", "at most"), null);

        //then
        //通过ArgumentCaptor捕获所有log
        ArgumentCaptor<ILoggingEvent> logCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
        verify(appender, times(LOG_COUNT)).doAppend(logCaptor.capture());

        assertThat(logCaptor.getAllValues())
                .hasSize(LOG_COUNT)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains(
                        "Found 4 elements of class java.lang.String",
                        "Repetitive elements: \nhello\nthree\nlogs\n..."
                );
    }
}