/*
 * Copyright © 2019-2024 Forb Yuan
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

package win.doyto.query.util;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.util.menu.MenuEntity;
import win.doyto.query.util.menu.MenuIdWrapper;
import win.doyto.query.util.menu.MenuRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BeanUtilTest
 *
 * @author f0rb on 2020-04-02
 */
class BeanUtilTest {
    MenuRequest menuRequest;
    MenuEntity menuEntity;

    @BeforeEach
    void setUp() {
        menuRequest = new MenuRequest();
        menuRequest.setId(1);
        menuRequest.setMenuName("submenu");

        menuEntity = new MenuEntity();
        menuEntity.setId(1);
        menuEntity.setMenuName("menu");
        menuEntity.setMemo("a menu");
        menuEntity.setParentId(0);
    }

    @Test
    void copyTo() {

        MenuEntity newMenuEntity = BeanUtil.copyTo(menuRequest, menuEntity);

        assertThat(newMenuEntity)
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("parentId", 0)
                .hasFieldOrPropertyWithValue("menuName", "submenu")
                .hasFieldOrPropertyWithValue("memo", null);

    }

    @Test
    void copyNonNull() {

        MenuEntity newMenuEntity = BeanUtil.copyNonNull(menuRequest, menuEntity);

        assertThat(newMenuEntity)
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("parentId", 0)
                .hasFieldOrPropertyWithValue("menuName", "submenu")
                .hasFieldOrPropertyWithValue("memo", "a menu");
    }

    @Test
    void parse() {
        MenuEntity menuEntity = BeanUtil.parse("{\"id\":\"1\"}", MenuEntity.class);
        assertThat(menuEntity)
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("parentId", null);
    }

    @Test
    void parseSingleAsList() {
        List<MenuEntity> menuEntities = BeanUtil.parse("{\"id\":\"1\"}", new TypeReference<List<MenuEntity>>() {});
        assertThat(menuEntities)
                .hasSize(1)
                .element(0)
                .hasFieldOrPropertyWithValue("id", 1);
    }

    @Test
    void stringify() {
        MenuIdWrapper menuIdWrapper = new MenuIdWrapper(1, "01");
        assertThat(BeanUtil.stringify(menuIdWrapper)).isEqualTo("{\"id\":1,\"platform\":\"01\"}");
    }

    @Test
    void convertTo() {
        MenuIdWrapper menuIdWrapper = new MenuIdWrapper();
        menuIdWrapper.setPlatform("01");

        MenuEntity menuEntity = BeanUtil.convertTo(menuIdWrapper, new TypeReference<MenuEntity>() {});
        IdWrapper<Integer> clonedIdWrapper = menuEntity.toIdWrapper();

        assertThat(clonedIdWrapper)
                .hasFieldOrPropertyWithValue("platform", "01")
                .hasFieldOrPropertyWithValue("id", null);
    }

    @Test
    void getIdClass() {
        assertThat(BeanUtil.getIdClass(MenuEntity.class)).isEqualTo(Integer.class);
    }
}
