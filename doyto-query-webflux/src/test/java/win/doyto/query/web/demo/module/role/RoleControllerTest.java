package win.doyto.query.web.demo.module.role;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
        roleController.create(roleEntities).subscribe();
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

    @Test
    void should_remove_the_entity_when_delete_given_existed_id() {
        roleController.delete(1)
                      .as(StepVerifier::create)
                      .assertNext(e -> assertThat(e)
                              .hasFieldOrPropertyWithValue("id", 1)
                              .hasFieldOrPropertyWithValue("roleName", "admin")
                      )
                      .verifyComplete();

        roleController.query(RoleQuery.builder().build())
                      .as(StepVerifier::create)
                      .expectNextMatches(e -> e.getId() == 2)
                      .expectNextMatches(e -> e.getId() == 3)
                      .verifyComplete();
    }


    @Test
    void should_return_null_when_delete_given_non_existed_id() {
        roleController.delete(-1)
                      .as(StepVerifier::create)
                      .expectNextCount(0)
                      .verifyComplete();

        roleController.query(RoleQuery.builder().build())
                      .as(StepVerifier::create)
                      .expectNextCount(3)
                      .verifyComplete();
    }

    @Test
    void update() {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(1);
        roleEntity.setRoleName("admin2");
        roleEntity.setValid(false);

        roleController.update(roleEntity).subscribe();

        roleController.get(1)
                      .as(StepVerifier::create)
                      .assertNext(e -> assertThat(e)
                              .hasFieldOrPropertyWithValue("id", 1)
                              .hasFieldOrPropertyWithValue("roleName", "admin2")
                              .hasFieldOrPropertyWithValue("roleCode", null)
                              .hasFieldOrPropertyWithValue("valid", false)
                      ).verifyComplete();
    }

    @Test
    void patch() {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(1);
        roleEntity.setRoleName("admin3");

        roleController.patch(roleEntity).subscribe();

        roleController.get(1)
                      .as(StepVerifier::create)
                      .assertNext(e -> assertThat(e)
                              .hasFieldOrPropertyWithValue("id", 1)
                              .hasFieldOrPropertyWithValue("roleName", "admin3")
                              .hasFieldOrPropertyWithValue("roleCode", "ADMIN")
                              .hasFieldOrPropertyWithValue("valid", true)
                      ).verifyComplete();
    }

    @Test
    void count() {
        roleController.count(RoleQuery.builder().build())
                      .as(StepVerifier::create)
                      .expectNext(3L)
                      .verifyComplete();
    }

    @Test
    void page() {
        roleController.page(RoleQuery.builder().pageNumber(1).pageSize(2).build())
                      .as(StepVerifier::create)
                      .assertNext(page -> {
                          assertThat(page.getTotal()).isEqualTo(3);
                          assertThat(page.getList())
                                  .hasSize(1)
                                  .extracting(RoleEntity::getId).containsExactly(3);
                      }).verifyComplete();
    }

    @Test
    void create() {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setRoleName("vip3");
        roleEntity.setValid(false);

        roleController.create(roleEntity)
                      .as(StepVerifier::create)
                      .assertNext(e -> assertThat(e.getId()).isEqualTo(4))
                      .verifyComplete();

        roleController.get(4)
                      .as(StepVerifier::create)
                      .assertNext(e -> assertThat(e)
                              .hasFieldOrPropertyWithValue("id", 4)
                              .hasFieldOrPropertyWithValue("roleName", "vip3")
                              .hasFieldOrPropertyWithValue("roleCode", null)
                              .hasFieldOrPropertyWithValue("valid", false)
                      ).verifyComplete();
    }
}
