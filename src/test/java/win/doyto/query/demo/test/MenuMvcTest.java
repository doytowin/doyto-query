package win.doyto.query.demo.test;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import win.doyto.query.demo.exception.ServiceException;

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
            assertTrue(e.getCause() instanceof ServiceException);
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void saveMenu() throws Exception {
        MvcResult mvcResult = mockMvc
                .perform(get(menuUri1))
                .andExpect(statusIs200())
                .andExpect(jsonPath("$.updateUserId").doesNotExist())
                .andReturn();
        String json = mvcResult.getResponse().getContentAsString();
        requestJson(put(menuUri1), json, session);
        mockMvc.perform(get(menuUri1))
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

        requestJson(post("/02/menu/batch"), "[{\"menuName\":\"Test Menu1\"},{\"menuName\":\"Test Menu2\"}]", session)
                .andExpect(statusIs200());

        mockMvc.perform(get("/02/menu/"))
               .andDo(print())
               .andExpect(statusIs200())
               .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void deleteMenu() throws Exception {
        try {
            mockMvc.perform(delete("/02/menu/0")).andDo(print());
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ServiceException);
        }
    }
}
