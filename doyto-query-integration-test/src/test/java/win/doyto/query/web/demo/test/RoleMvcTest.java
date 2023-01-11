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

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.RequestBuilder;
import win.doyto.query.test.role.RoleEntity;
import win.doyto.query.util.BeanUtil;
import win.doyto.query.web.response.JsonResponse;

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
                .andExpect(jsonPath("$.data.total").value(6))
        ;
    }

    @Test
    @Rollback
    void patchRole() throws Exception {
        RequestBuilder requestBuilder = patch("/role/2")
                .content("{\"id\":2,\"roleName\":\"new role\"}").contentType(MediaType.APPLICATION_JSON);
        performAndExpectSuccess(requestBuilder);
        performAndExpectSuccess(get("/role/2"))
                .andExpect(jsonPath("$.data.roleName").value("new role"))
        ;
    }

    @Test
    @Rollback
    void updateRole() throws Exception {
        String role2 = performAndExpectSuccess(get("/role/2"))
                .andReturn().getResponse().getContentAsString();
        RoleEntity patch = BeanUtil.parse(role2, new TypeReference<JsonResponse<RoleEntity>>() {}).getData();
        patch.setRoleName("vvip");

        RequestBuilder requestBuilder = put("/role/2")
                .content(BeanUtil.stringify(patch)).contentType(MediaType.APPLICATION_JSON);
        performAndExpectSuccess(requestBuilder);
        performAndExpectSuccess(get("/role/2"))
                .andExpect(jsonPath("$.data.roleName").value("vvip"))
                .andExpect(jsonPath("$.data.roleCode").value("VIP"))
        ;
    }

    @Test
    @Rollback
    void deleteById() throws Exception {
        performAndExpectSuccess(delete("/role/1"));
        performAndExpectSuccess(get("/role/?pageSize=1"))
                .andExpect(jsonPath("$.data.total").value(4))
                .andExpect(jsonPath("$.data.list[0].id").value(2))
        ;
    }

    @Test
    @Rollback
    void batch() throws Exception {
        String data = "[{\"roleName\":\"vip5\",\"roleCode\":\"VIP5\"},{\"roleName\":\"vip6\",\"roleCode\":\"VIP6\"}]";
        performAndExpectSuccess(post("/role").content(data).contentType(MediaType.APPLICATION_JSON));
        performAndExpectSuccess(get("/role/?pageSize=1"))
                .andExpect(jsonPath("$.data.total").value(7))
        ;
    }
}
