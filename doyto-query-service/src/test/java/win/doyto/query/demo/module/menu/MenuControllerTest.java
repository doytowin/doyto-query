package win.doyto.query.demo.module.menu;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.util.BeanUtil;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MenuControllerTest
 *
 * @author f0rb
 */
class MenuControllerTest {
    String platform = "01";
    MenuController menuController = new MenuController(new MenuService());

    @BeforeEach
    void setUp() throws IOException {
        menuController.create(BeanUtil.loadJsonData("menu.json", new TypeReference<List<MenuRequest>>() {}));
    }

    @Test
    void get() {
        MenuIdWrapper menuIdWrapper = new MenuIdWrapper(1, platform);
        MenuResponse menuResponse = menuController.get(menuIdWrapper);
        assertEquals("root", menuResponse.getMenuName());
        assertEquals(0, (int) menuResponse.getParentId());
    }

}