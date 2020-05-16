package win.doyto.query.web.demo.test;

import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.Rollback;
import win.doyto.query.web.response.PresetErrorCode;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * MenuMvcTest
 *
 * @author f0rb on 2020-04-11
 */
@Rollback
class MenuMvcTest extends DemoApplicationTest {

    private String menuUri = "/01/menu/";
    private String menuUri1 = menuUri + "1";

    @Test
    void pageMenu() throws Exception {
        performAndExpectSuccess(get(menuUri + "?pageNumber=1&pageSize=1"))
                .andExpect(jsonPath("$.data.list.size()").value(1))
                .andExpect(jsonPath("$.data.total").value(2))
        ;
    }

    @Test
    void getMenuById() throws Exception {
        performAndExpectOk(get(menuUri1))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.menuName").value("root"))
        ;

        performAndExpectFail(get(menuUri + "9"), PresetErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @Rollback
    void saveMenu() throws Exception {
        performAndExpectSuccess(get(menuUri1))
                .andExpect(jsonPath("$.data.updateUserId").doesNotExist())
                .andReturn();
        performAndExpectSuccess(patch(menuUri), "{\"id\":1,\"platform\":\"01\",\"memo\":\"new memo\"}");
        performAndExpectSuccess(get(menuUri1))
                .andExpect(jsonPath("$.data.memo").value("new memo"))
                .andExpect(jsonPath("$.data.updateUserId").value("1"));
    }

    @Test
    void createMenu() throws Exception {
        performAndExpectFail(buildJson(post(menuUri), "{\"menuName\":\"Test Menu\"}"), PresetErrorCode.ARGUMENT_VALIDATION_FAILED);

        performAndExpectSuccess(post(menuUri), "{\"platform\":\"01\",\"menuName\":\"Test Menu\"}");

        performAndExpectSuccess(get(menuUri + "3"))
                .andExpect(jsonPath("$.data.createUserId").value("1"))
                .andExpect(jsonPath("$.data.updateUserId").value("1"));

        performAndExpectSuccess(delete(menuUri + "3"));

        performAndExpectSuccess(get(menuUri + "?pageNumber=0"))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void createMenus() throws Exception {
        String content = "[{\"platform\":\"02\",\"menuName\":\"Test Menu1\"},{\"platform\":\"02\",\"menuName\":\"Test Menu2\"}]";
        performAndExpectSuccess(post("/02/menu/"), content);
        performAndExpectOk(get("/02/menu/"))
                .andExpect(jsonPath("$.data.total").value(3));
    }

    @Test
    void deleteMenu() throws Exception {
        performAndExpectFail(delete("/02/menu/0"), PresetErrorCode.ENTITY_NOT_FOUND);

        performAndExpectSuccess(delete("/02/menu/1"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.platform").value("02"));
    }

    @Test
    void updateMenu() throws Exception {
        performAndExpectSuccess(get(menuUri1))
                .andExpect(jsonPath("$.data.updateUserId").doesNotExist())
                .andReturn();
        performAndExpectSuccess(put(menuUri), "{\"id\":1,\"platform\":\"01\",\"memo\":\"new memo\"}");
        performAndExpectSuccess(get(menuUri1))
                .andExpect(jsonPath("$.data.memo").value("new memo"))
                .andExpect(jsonPath("$.data.updateUserId").value("1"));
    }
}
