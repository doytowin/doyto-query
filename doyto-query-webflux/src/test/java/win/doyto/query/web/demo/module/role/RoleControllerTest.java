package win.doyto.query.web.demo.module.role;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import win.doyto.query.util.BeanUtil;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RoleControllerTest
 *
 * @author f0rb on 2021-10-26
 */
class RoleControllerTest {

    private RoleController roleController;

    @BeforeEach
    void setUp() throws IOException {
        roleController = new RoleController();
        List<RoleEntity> roleEntities = BeanUtil.loadJsonData("role.json", new TypeReference<List<RoleEntity>>() {});
        Flux<Object> flux = Flux.empty();
        for (RoleEntity roleEntity : roleEntities) {
            flux = flux.mergeWith(roleController.add(roleEntity));
        }
        flux.as(StepVerifier::create)
            .expectNextCount(3)
            .verifyComplete();
    }

    @Test
    void query() {
        roleController.query(RoleQuery.builder().build())
                      .as(StepVerifier::create)
                      .expectNextMatches(e -> e.getId() == 1)
                      .expectNextMatches(e -> e.getId() == 2)
                      .expectNextMatches(e -> e.getId() == 3)
                      .verifyComplete();
    }

    @Test
    void get() {
        roleController.get(1)
                      .as(StepVerifier::create)
                      .assertNext(e -> assertThat(e)
                              .hasFieldOrPropertyWithValue("id", 1)
                              .hasFieldOrPropertyWithValue("roleName", "admin")
                      )
                      .verifyComplete();
    }

}
