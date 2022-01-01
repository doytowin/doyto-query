/*
 * Copyright © 2019-2022 Forb Yuan
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.service.PageList;
import win.doyto.query.test.role.RoleEntity;
import win.doyto.query.test.role.RoleQuery;
import win.doyto.query.util.BeanUtil;
import win.doyto.query.web.demo.module.role.RoleController;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * roleControllerTest
 *
 * @author f0rb on 2020-04-01
 */
class RoleControllerTest {

    private RoleController roleController;

    @BeforeEach
    void setUp() throws IOException {
        roleController = new RoleController();
        roleController.create(BeanUtil.loadJsonData("/role.json", new TypeReference<List<RoleEntity>>() {}));
    }

    @Test
    void page() {
        PageList<RoleEntity> roleEntities = roleController.page(RoleQuery.builder().pageNumber(1).pageSize(2).build());
        assertEquals(1, roleEntities.getList().size());
        assertEquals(3, roleEntities.getTotal());
    }

    @Test
    void add() {
        RoleEntity roleEntity = new RoleEntity();
        roleController.create(roleEntity);
        assertEquals(4, roleController.count(RoleQuery.builder().build()));
    }
}
