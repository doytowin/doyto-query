package win.doyto.query.data;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import win.doyto.query.core.SqlAndArgs;

import java.util.ArrayList;

/**
 * R2dbcTemplateTest
 *
 * @author f0rb on 2021-11-20
 */
class R2dbcTemplateTest {

    @Test
    void count() {
        R2dbcOperations r2dbc = new R2dbcTemplate();
        r2dbc.count(new SqlAndArgs("SELECT count(*) FROM t_role", new ArrayList<>()))
             .as(StepVerifier::create)
             .expectNext(3L)
             .verifyComplete();
    }
}
