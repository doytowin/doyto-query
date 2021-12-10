package win.doyto.query.util;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.test.DynamicEntity;
import win.doyto.query.test.DynamicIdWrapper;
import win.doyto.query.test.TestEntity;
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
        DynamicIdWrapper dynamicIdWrapper = new DynamicIdWrapper();
        dynamicIdWrapper.setId(1);
        dynamicIdWrapper.setUser("test");
        dynamicIdWrapper.setProject("i18n");
        DynamicEntity dynamicEntity = BeanUtil.convertTo(dynamicIdWrapper, new TypeReference<DynamicEntity>() {});
        assertThat(dynamicEntity.toIdWrapper())
                .isEqualToComparingFieldByField(dynamicIdWrapper)
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("user", "test")
                .hasFieldOrPropertyWithValue("project", "i18n");
    }

    @Test
    void getIdClass() {
        assertThat(BeanUtil.getIdClass(TestEntity.class)).isEqualTo(Integer.class);
    }
}