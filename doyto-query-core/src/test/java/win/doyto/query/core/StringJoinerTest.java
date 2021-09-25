package win.doyto.query.core;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * StringJoinerTest
 *
 * @author f0rb on 2019-06-05
 */
class StringJoinerTest {

    private static final int TIMES = 50000;

    @Test
    void join() {
        String expected = "INSERT INTO user (username, password) VALUES (?, ?)";
        String actual = new StringJoiner(" ", 5)
            .append("INSERT INTO")
            .append("user")
            .append("(username, password)")
            .append("VALUES")
            .append("(?, ?)")
            .toString();
        assertEquals(expected, actual);
    }

    @Test
    void joinEmpty() {
        assertEquals("", new StringJoiner(" ", 5).toString());
    }

    @Test
    void joinNotFull() {
        assertEquals("test", new StringJoiner(",", 5).append("test").toString());
    }

    @Test
    void timeCost() {
        String separator = " ";
        int size = 5;

        Invocable<String> runJoiner = () -> new StringJoiner(separator, size)
            .append("INSERT INTO")
            .append("user")
            .append("(username, password)")
            .append("VALUES")
            .append("(?, ?)")
            .toString();
        runJoiner.invoke();

        Invocable<String> runJoinList = () -> {
            ArrayList<String> insertList = new ArrayList<>(size);
            insertList.add("INSERT INTO");
            insertList.add("user");
            insertList.add("(username, password)");
            insertList.add("VALUES");
            insertList.add("(?, ?)");
            return StringUtils.join(insertList, separator);
        };
        runJoinList.invoke();

        long joinerCost = TimeRecorder.run(runJoiner);
        System.out.println("Time for StringJoiner :" + joinerCost);

        long joinCost = TimeRecorder.run(runJoinList);
        System.out.println("Time for StringUtils.join :" + joinCost);
        // jacoco对性能测试有影响, 无法保证 joinerCost < joinCost
        assertTrue(joinerCost > 0);
    }

    static class TimeRecorder {
        static long run(Invocable<String> invocable) {
            long start = System.currentTimeMillis();
            IntStream.range(0, TIMES).forEachOrdered(i -> invocable.invoke());
            return System.currentTimeMillis() - start;
        }
    }

}