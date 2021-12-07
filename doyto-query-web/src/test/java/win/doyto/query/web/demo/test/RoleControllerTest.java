package win.doyto.query.web.demo.test;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.service.PageList;
import win.doyto.query.util.BeanUtil;
import win.doyto.query.web.demo.module.role.RoleController;
import win.doyto.query.web.demo.module.role.RoleEntity;
import win.doyto.query.web.demo.module.role.RoleQuery;

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
