package win.doyto.query.module.menu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MenuControllerTest
 *
 * @author f0rb
 */
class MenuControllerTest {
    String platform = "01";

    @Test
    void get() {
        MenuController menuController = new MenuController();

        MenuRequest menuRequest = new MenuRequest();
        menuRequest.setParentId(0);
        menuRequest.setMenuName("root");
        menuController.create(menuRequest, platform);

        MenuQuery menuQuery = new MenuQuery();
        menuQuery.setId(1);
        menuQuery.setPlatform(platform);
        MenuResponse menuResponse = menuController.getByQuery(menuQuery);
        assertEquals("root", menuResponse.getMenuName());
        assertEquals(0, (int) menuResponse.getParentId());
    }

}