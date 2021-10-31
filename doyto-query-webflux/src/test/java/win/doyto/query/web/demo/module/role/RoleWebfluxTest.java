package win.doyto.query.web.demo.module.role;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import win.doyto.query.util.BeanUtil;
import win.doyto.query.web.demo.WebfluxApplication;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Resource;

/**
 * RoleWebfluxTest
 *
 * @author f0rb on 2021-10-30
 */
@SpringBootTest(classes = WebfluxApplication.class)
@AutoConfigureWebTestClient
class RoleWebfluxTest {

    @Resource
    protected WebTestClient webTestClient;

    private Consumer<EntityExchangeResult<byte[]>> log() {
        return entityExchangeResult -> System.out.println(entityExchangeResult.toString());
    }

    @BeforeEach
    void setUp() throws IOException {
        List<RoleEntity> roleEntities = BeanUtil.loadJsonData("role.json", new TypeReference<List<RoleEntity>>() {});
        webTestClient.post().uri("/role/")
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(BodyInserters.fromValue(roleEntities))
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody()
                     .jsonPath("$.success").isEqualTo(true);
    }

    @Test
    void getById() {
        webTestClient.get().uri("/role/1")
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody()
                     .consumeWith(log())
                     .jsonPath("$.success").isEqualTo(true)
                     .jsonPath("$.data.id").isEqualTo("1")
                     .jsonPath("$.data.roleName").isEqualTo("admin");
    }

    @Test
    void deleteById() {
        webTestClient.delete().uri("/role/1")
                     .exchange()
                     .expectBody()
                     .jsonPath("$.success").isEqualTo(true);
    }

    @Test
    void should_return_code_9_when_get_nonexistent_entity() {
        webTestClient.get().uri("/role/-1")
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody()
                     .consumeWith(log())
                     .jsonPath("$.success").isEqualTo(false)
                     .jsonPath("$.code").isEqualTo(9)
                     .jsonPath("$.message").isEqualTo("查询记录不存在");
    }
}
