package win.doyto.query.util;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.demo.module.menu.MenuEntity;
import win.doyto.query.demo.module.menu.MenuRequest;

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
    void load() {
        List<MenuEntity> menuEntities = BeanUtil.parse("{\"id\":\"1\"}", new TypeReference<List<MenuEntity>>() {});
        assertThat(menuEntities)
                .hasSize(1)
                .element(0)
                .hasFieldOrPropertyWithValue("id", 1);
    }
}