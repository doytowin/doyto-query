package win.doyto.query.util;

import org.junit.jupiter.api.Test;
import win.doyto.query.demo.module.menu.MenuEntity;
import win.doyto.query.demo.module.menu.MenuRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * BeanUtilTest
 *
 * @author f0rb on 2020-04-02
 */
class BeanUtilTest {

    @Test
    void copyTo() {
        MenuRequest menuRequest = new MenuRequest();
        menuRequest.setId(1);
        menuRequest.setMenuName("submenu");

        MenuEntity menuEntity = new MenuEntity();
        menuEntity.setId(1);
        menuEntity.setMenuName("menu");
        menuEntity.setMemo("a menu");

        MenuEntity newMenuEntity = BeanUtil.copyTo(menuRequest, menuEntity);

        assertEquals(Integer.valueOf(1), newMenuEntity.getId());
        assertEquals("submenu", newMenuEntity.getMenuName());
        assertNull(newMenuEntity.getMemo());

    }
}