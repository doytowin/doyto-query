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
    void timeCost() throws Exception {
        String separator = " ";
        int size = 5;

        Runnable runJoiner = () -> new StringJoiner(separator, size)
                .append("INSERT INTO")
                .append("user")
                .append("(username, password)")
                .append("VALUES")
                .append("(?, ?)")
                .toString();
        runJoiner.run();

        Runnable runJoinList = () -> {
            ArrayList<String> insertList = new ArrayList<>(size);
            insertList.add("INSERT INTO");
            insertList.add("user");
            insertList.add("(username, password)");
            insertList.add("VALUES");
            insertList.add("(?, ?)");
            StringUtils.join(insertList, separator);
        };
        runJoinList.run();

        long joinerCost = TimeRecorder.run(runJoiner);
        System.out.println("Time for StringJoiner :" + joinerCost);

        long joinCost = TimeRecorder.run(runJoinList);
        System.out.println("Time for StringUtils.join :" + joinCost);
        // jacoco对性能测试有影响, 无法保证 joinerCost < joinCost
        assertTrue(joinerCost > 0);
    }

    static class TimeRecorder {
        static long run(Runnable runnable) {
            long start = System.currentTimeMillis();
            IntStream.range(0, TIMES).forEachOrdered(i -> runnable.run());
            return System.currentTimeMillis() - start;
        }
    }

}