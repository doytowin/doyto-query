package win.doyto.query.demo.module.role;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RoleControllerTest
 *
 * @author f0rb on 2019-06-26
 */
class RoleControllerTest {

    @Test
    void generatedIdTypeShouldMatchGenericTypeParameterForCommonEntity() {
        RoleController roleController = new RoleController();

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setRoleName("root");
        roleController.add(roleEntity);

        roleController.query(new RoleQuery());
        assertEquals(Long.valueOf(1), roleEntity.getId());
    }

}