package win.doyto.query.web.demo.test;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.RequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * RoleMvcTest
 *
 * @author f0rb on 2020-04-02
 */
class RoleMvcTest extends DemoApplicationTest {

    @Test
    void getById() throws Exception {
        performAndExpectSuccess(get("/role/1"))
                .andExpect(jsonPath("$.data.roleName").value("admin"))
        ;
    }

    @Test
    void queryByRoleName() throws Exception {
        performAndExpectSuccess(get("/role/?roleNameLike=admin"))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list[0].roleCode").value("ADMIN"))
        ;
    }

    @Test
    @Rollback
    void add() throws Exception {
        String content = "{\"roleName\":\"test5\",\"roleCode\":\"VIP1\",\"valid\":true}";
        RequestBuilder requestBuilder = post("/role/").content(content).contentType(MediaType.APPLICATION_JSON);
        performAndExpectSuccess(requestBuilder);
        performAndExpectSuccess(get("/role/?pageSize=1"))
                .andExpect(jsonPath("$.data.total").value(4))
        ;
    }

    @Test
    @Rollback
    void patchRole() throws Exception {
        RequestBuilder requestBuilder = patch("/role/2").content("{\"roleName\":\"new role\"}").contentType(MediaType.APPLICATION_JSON);
        performAndExpectSuccess(requestBuilder);
        performAndExpectSuccess(get("/role/2"))
                .andExpect(jsonPath("$.data.roleName").value("new role"))
        ;
    }

    @Test
    @Rollback
    void updateRole() throws Exception {
        RequestBuilder requestBuilder = put("/role/2").content("{\"roleName\":\"vip3\",\"roleCode\":\"VIP3\"}").contentType(MediaType.APPLICATION_JSON);
        performAndExpectSuccess(requestBuilder);
        performAndExpectSuccess(get("/role/2"))
                .andExpect(jsonPath("$.data.roleName").value("vip3"))
                .andExpect(jsonPath("$.data.roleCode").value("VIP3"))
        ;
    }

    @Test
    @Rollback
    void deleteById() throws Exception {
        performAndExpectSuccess(delete("/role/1"));
        performAndExpectSuccess(get("/role/?pageSize=1"))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list[0].id").value(2))
        ;
    }

    @Test
    @Rollback
    void batch() throws Exception {
        performAndExpectSuccess(post("/role").content("[{\"roleName\":\"vip3\",\"roleCode\":\"VIP3\"},{\"roleName\":\"vip4\",\"roleCode\":\"VIP4\"}]").contentType(MediaType.APPLICATION_JSON));
        performAndExpectSuccess(get("/role/?pageSize=1"))
                .andExpect(jsonPath("$.data.total").value(5))
        ;
    }
}
