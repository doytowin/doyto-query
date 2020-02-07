package win.doyto.query.demo.module.menu;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.demo.common.BeanUtil;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MenuControllerTest
 *
 * @author f0rb
 */
class MenuControllerTest {
    String platform = "01";
    MenuController menuController = new MenuController();

    @BeforeEach
    void setUp() throws IOException {
        menuController.batchInsert(BeanUtil.loadJsonData("menu.json", new TypeReference<Iterable<MenuEntity>>() {}));
    }

    @Test
    void get() {
        MenuRequest menuRequest = new MenuRequest();
        menuRequest.setId(1);
        menuRequest.setPlatform(platform);
        MenuResponse menuResponse = menuController.get(menuRequest);
        assertEquals("root", menuResponse.getMenuName());
        assertEquals(0, (int) menuResponse.getParentId());
    }

}