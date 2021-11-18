package win.doyto.query.data;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import win.doyto.query.web.demo.module.role.RoleEntity;
import win.doyto.query.web.demo.module.role.RoleQuery;

/**
 * ReactiveDefaultDataAccessTest
 *
 * @author f0rb on 2021-11-18
 */
class ReactiveDatabaseDataAccessTest {
    @Test
    void count() {
        ReactiveDataAccess<RoleEntity, Integer, RoleQuery> reactiveDefaultDataAccess = new ReactiveDatabaseDataAccess<>();
        Mono<Long> roleCount = reactiveDefaultDataAccess.count(RoleQuery.builder().build());
        roleCount.as(StepVerifier::create)
                 .expectNext(3L)
                 .verifyComplete();
    }
}