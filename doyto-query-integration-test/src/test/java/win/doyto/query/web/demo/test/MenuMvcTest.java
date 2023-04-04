/*
 * Copyright © 2019-2023 Forb Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.doyto.query.web.demo.test;

import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import win.doyto.query.web.response.PresetErrorCode;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * MenuMvcTest
 *
 * @author f0rb on 2020-04-11
 */
class MenuMvcTest extends DemoApplicationTest {

    private final String menuUri = "/01/menu/";
    private final String menuUri1 = menuUriWith("1");

    private String menuUriWith(String str) {
        return menuUri + str;
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void pageMenu() throws Exception {
        performAndExpectSuccess(get(menuUriWith("?page=1&size=1")))
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

        performAndExpectFail(get(menuUriWith("9")), PresetErrorCode.ENTITY_NOT_FOUND);
    }

    @Test
    @Rollback
    void saveMenu() throws Exception {
        performAndExpectSuccess(get(menuUri1))
                .andExpect(jsonPath("$.data.updateUserId").doesNotExist())
                .andReturn();
        performAndExpectSuccess(patch(menuUri1), "{\"id\":1,\"platform\":\"01\",\"memo\":\"new memo\"}");
        performAndExpectSuccess(get(menuUri1))
                .andExpect(jsonPath("$.data.memo").value("new memo"))
                .andExpect(jsonPath("$.data.updateUserId").value("1"));
    }

    @Test
    void createMenu() throws Exception {
        performAndExpectFail(buildJson(post(menuUri), "{\"menuName\":\"Test Menu\"}"), PresetErrorCode.ARGUMENT_VALIDATION_FAILED);

        performAndExpectSuccess(post(menuUri), "{\"platform\":\"01\",\"menuName\":\"Test Menu\"}");

        performAndExpectSuccess(get(menuUriWith("3")))
                .andExpect(jsonPath("$.data.createUserId").value("1"))
                .andExpect(jsonPath("$.data.updateUserId").value("1"));

        performAndExpectSuccess(delete(menuUriWith("3")));

        performAndExpectSuccess(get(menuUriWith("?pageNumber=0")))
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
        performAndExpectFail(delete("/00/menu/0"), PresetErrorCode.ENTITY_NOT_FOUND);

        performAndExpectSuccess(delete("/00/menu/1"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.platform").value("00"));
    }

    @Test
    void updateMenu() throws Exception {
        performAndExpectSuccess(get(menuUri1))
                .andExpect(jsonPath("$.data.updateUserId").doesNotExist())
                .andReturn();
        performAndExpectSuccess(put(menuUri1), "{\"id\":1,\"platform\":\"01\",\"memo\":\"new memo\"}");
        performAndExpectSuccess(get(menuUri1))
                .andExpect(jsonPath("$.data.menuName").doesNotExist())
                .andExpect(jsonPath("$.data.memo").value("new memo"))
                .andExpect(jsonPath("$.data.updateUserId").value("1"));
    }
}
