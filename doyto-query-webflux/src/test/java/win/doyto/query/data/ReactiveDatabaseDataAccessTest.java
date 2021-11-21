package win.doyto.query.data;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.SqlAndArgs;
import win.doyto.query.web.demo.module.role.RoleEntity;
import win.doyto.query.web.demo.module.role.RoleQuery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ReactiveDefaultDataAccessTest
 *
 * @author f0rb on 2021-11-18
 */
class ReactiveDatabaseDataAccessTest {
    @BeforeAll
    static void beforeAll() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(true);
    }

    @AfterAll
    static void afterAll() {
        GlobalConfiguration.instance().setMapCamelCaseToUnderscore(false);
    }

    @Test
    void count() {
        R2dbcOperations mockR2dbc = mock(R2dbcOperations.class);
        when(mockR2dbc.count(any())).thenReturn(Mono.just(3L));

        ReactiveDataAccess<RoleEntity, Integer, RoleQuery> reactiveDataAccess =
                new ReactiveDatabaseDataAccess<>(mockR2dbc, RoleEntity.class);
        reactiveDataAccess.count(RoleQuery.builder().build())
                          .as(StepVerifier::create)
                          .expectNext(3L)
                          .verifyComplete();

        ArgumentCaptor<SqlAndArgs> argumentCaptor = ArgumentCaptor.forClass(SqlAndArgs.class);
        verify(mockR2dbc, times(1)).count(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getSql()).isEqualTo("SELECT count(*) FROM t_role");
        assertThat(argumentCaptor.getValue().getArgs()).isEmpty();
    }

    @Test
    void query() {
        R2dbcTemplate r2dbcTemplate = R2dbcTemplateTest.createR2dbcTemplate();

        ReactiveDataAccess<RoleEntity, Integer, RoleQuery> reactiveDataAccess =
                new ReactiveDatabaseDataAccess<>(r2dbcTemplate, RoleEntity.class);

        reactiveDataAccess.query(RoleQuery.builder().build())
                          .as(StepVerifier::create)
                          .expectNextMatches(roleEntity -> roleEntity.getId() == 1
                                  && roleEntity.getRoleName().equals("admin")
                                  && roleEntity.getRoleCode().equals("ADMIN")
                                  && roleEntity.getValid())
                          .expectNextCount(2)
                          .verifyComplete();
    }
}