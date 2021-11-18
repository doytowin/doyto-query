package win.doyto.query.data;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import win.doyto.query.web.demo.module.role.RoleEntity;
import win.doyto.query.web.demo.module.role.RoleQuery;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * ReactiveDefaultDataAccessTest
 *
 * @author f0rb on 2021-11-18
 */
class ReactiveDatabaseDataAccessTest {
    @Test
    void count() {
        R2dbcOperations mockR2dbc = mock(R2dbcOperations.class);
        when(mockR2dbc.count(any())).thenReturn(Mono.just(3L));

        ReactiveDataAccess<RoleEntity, Integer, RoleQuery> reactiveDataAccess =
                new ReactiveDatabaseDataAccess<>(mockR2dbc);
        reactiveDataAccess.count(RoleQuery.builder().build())
                          .as(StepVerifier::create)
                          .expectNext(3L)
                          .verifyComplete();
    }
}