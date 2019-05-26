package win.doyto.query.module.menu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MenuControllerTest
 *
 * @author f0rb
 */
class MenuControllerTest {

    @Test
    void get() {
        MenuController menuController = new MenuController(new MenuService());

        MenuRequest menuRequest = new MenuRequest();
        menuRequest.setParentId(0);
        menuRequest.setMenuName("root");
        menuController.create(menuRequest);

        MenuResponse menuResponse = menuController.get(1);
        assertEquals("root", menuResponse.getMenuName());
        assertEquals(0, (int) menuResponse.getParentId());
    }

}