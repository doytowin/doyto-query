package win.doyto.query.demo.test;

import org.junit.jupiter.api.Test;
import win.doyto.query.web.response.ErrorCodeException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * RoleMvcTest
 *
 * @author f0rb on 2020-04-11
 */
class RoleMvcTest extends DemoApplicationTest {

    @Test
    void queryRole() throws Exception {
        mockMvc.perform(get("/role"))
               .andExpect(statusIs200())
               .andExpect(jsonPath("$.list[0].roleName").value("测试"));
        mockMvc.perform(get("/role?pageNumber=0&pageSize=5"))
               .andExpect(statusIs200())
               .andExpect(jsonPath("$.list[0].roleName").value("测试"))
               .andExpect(jsonPath("$.list[1].roleName").value("高级"))
               .andExpect(jsonPath("$.total").value(2))
        ;
    }

    @Test
    void deleteRole() throws Exception {
        mockMvc.perform(delete("/role/1")).andExpect(statusIs200());
        try {
            mockMvc.perform(delete("/role/0"));
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ErrorCodeException);
        }
    }

    @Test
    void createRole() throws Exception {
        requestJson(post("/role/"), "{\"roleName\":\"超级\",\"roleCode\":\"VVIP\",\"valid\":true}", session)
                .andExpect(statusIs200());
        mockMvc.perform(get("/role/3"))
               .andExpect(jsonPath("$.roleCode").value("VVIP"))
               .andExpect(jsonPath("$.createUserId").value(1))
               .andExpect(jsonPath("$.updateUserId").value(1));
    }

    @Test
    void patchAndUpdate() throws Exception {
        requestJson(patch("/role/2"), "{\"id\":2,\"roleName\":\"超级\",\"roleCode\":\"VVIP\"}", session)
                .andExpect(statusIs200());
        mockMvc.perform(get("/role/2"))
               .andExpect(jsonPath("$.roleCode").value("VVIP"))
               .andExpect(jsonPath("$.roleName").value("超级"))
               .andExpect(jsonPath("$.valid").value(true))
        ;

        requestJson(put("/role/2"), "{\"id\":2,\"roleName\":\"超级\",\"roleCode\":\"VVVIP\"}", session)
                .andExpect(statusIs200());
        mockMvc.perform(get("/role/2"))
               .andExpect(jsonPath("$.roleCode").value("VVVIP"))
               .andExpect(jsonPath("$.roleName").value("超级"))
               .andExpect(jsonPath("$.valid").doesNotExist())
        ;
    }
}
