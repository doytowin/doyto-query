package win.doyto.query.web.demo.module.role;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import win.doyto.query.web.demo.WebfluxApplication;

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

    @Test
    void getById() {
        webTestClient.get().uri("/role/1")
                     .exchange()
                     .expectStatus().isOk();
    }

}
