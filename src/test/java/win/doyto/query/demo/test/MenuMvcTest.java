package win.doyto.query.demo.test;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import win.doyto.query.web.response.ErrorCodeException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * MenuMvcTest
 *
 * @author f0rb on 2020-04-11
 */
class MenuMvcTest extends DemoApplicationTest {

    private String menuUri = "/01/menu/";
    private String menuUri1 = menuUri + "1";

    @Test
    void pageMenu() throws Exception {
        mockMvc.perform(get(menuUri + "?pageNumber=1&pageSize=2"))
               .andExpect(statusIs200())
               .andExpect(jsonPath("$.list").isArray())
               .andExpect(jsonPath("$.list[0]").doesNotExist())
               .andExpect(jsonPath("$.total").value(2))
        ;
    }

    @Test
    void getMenuById() throws Exception {
        mockMvc.perform(get(menuUri1))
               .andDo(print())
               .andExpect(statusIs200())
               .andExpect(jsonPath("$.id").value("1"))
               .andExpect(jsonPath("$.menuName").value("root"))
        ;

        try {
            mockMvc.perform(get(menuUri + "9"));
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ErrorCodeException);
        }
    }

    @Test
    void saveMenu() throws Exception {
        MvcResult mvcResult = mockMvc
                .perform(get(menuUri1))
                .andExpect(statusIs200())
                .andExpect(jsonPath("$.updateUserId").doesNotExist())
                .andReturn();
        String json = mvcResult.getResponse().getContentAsString();
        requestJson(put(menuUri), json, session)
                .andExpect(statusIs200());
        mockMvc.perform(get(menuUri1))
               .andDo(print())
               .andExpect(statusIs200())
               .andExpect(jsonPath("$.updateUserId").value("1"));
    }

    @Test
    void createMenu() throws Exception {
        requestJson(post(menuUri), "{\"platform\":\"01\", \"menuName\":\"Test Menu\"}", session)
                .andExpect(statusIs200());

        mockMvc.perform(get(menuUri + "3"))
               .andExpect(statusIs200())
               .andExpect(jsonPath("$.createUserId").value("1"))
               .andExpect(jsonPath("$.updateUserId").value("1"));

        mockMvc.perform(delete(menuUri + "3")).andExpect(statusIs200());

        mockMvc.perform(get(menuUri + "?pageNumber=0"))
               .andExpect(statusIs200())
               .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void createMenus() throws Exception {

        requestJson(post("/02/menu/"), "[{\"platform\":\"02\",\"menuName\":\"Test Menu1\"},{\"platform\":\"02\",\"menuName\":\"Test Menu2\"}]", session)
                .andExpect(statusIs200());

        mockMvc.perform(get("/02/menu/"))
               .andDo(print())
               .andExpect(statusIs200())
               .andExpect(jsonPath("$.list.length()").value(3));
    }

    @Test
    void deleteMenu() {
        try {
            mockMvc.perform(delete("/02/menu/0"));
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ErrorCodeException);
        }
    }
}
