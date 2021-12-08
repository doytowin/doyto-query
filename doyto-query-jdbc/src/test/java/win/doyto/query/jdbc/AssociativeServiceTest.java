package win.doyto.query.jdbc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import win.doyto.query.service.AssociativeService;

import java.util.Arrays;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.*;

/**
 * AssociativeServiceTest
 *
 * @author f0rb on 2020-04-11
 */
class AssociativeServiceTest extends JdbcApplicationTest {

    @Autowired
    @Qualifier("userAndRoleAssociativeService")
    AssociativeService<Long, Integer> userAndRoleAssociativeService;

    @Test
    void associativeService$count() {
        assertEquals(0L, userAndRoleAssociativeService.count(emptyList(), emptyList()));
    }

    @Test
    void associativeService$exists() {
        assertTrue(userAndRoleAssociativeService.exists(1L, 1));

        assertTrue(userAndRoleAssociativeService.exists(1L, Arrays.asList(1, 2)));
        assertArrayEquals(new Object[]{1, 2}, userAndRoleAssociativeService.getByLeftId(1L).toArray());
        assertArrayEquals(new Object[]{1L, 4L}, userAndRoleAssociativeService.getByRightId(2).toArray());

        userAndRoleAssociativeService.deallocate(1L, 1);
        assertTrue(userAndRoleAssociativeService.exists(1L, Arrays.asList(1, 2)));
        assertFalse(userAndRoleAssociativeService.exists(singleton(1L), 1));

        userAndRoleAssociativeService.allocate(1L, 1);
        assertTrue(userAndRoleAssociativeService.exists(singleton(1L), 1));
    }

    @Test
    void associativeService$reallocate() {
        assertTrue(userAndRoleAssociativeService.exists(1L, 1));

        assertEquals(2, userAndRoleAssociativeService.reallocateForLeft(1L, Arrays.asList(2, 3)));
        assertFalse(userAndRoleAssociativeService.exists(1L, 1));
        assertTrue(userAndRoleAssociativeService.exists(1L, Arrays.asList(1, 2)));
        assertTrue(userAndRoleAssociativeService.exists(1L, Arrays.asList(2, 3)));

        assertEquals(0, userAndRoleAssociativeService.reallocateForRight(2, emptyList()));
        assertEquals(0, userAndRoleAssociativeService.reallocateForRight(3, emptyList()));
        assertFalse(userAndRoleAssociativeService.exists(1L, Arrays.asList(1, 2, 3)));
    }

}
