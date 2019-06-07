package win.doyto.query.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.core.test.TestEntity;
import win.doyto.query.core.test.TestEnum;
import win.doyto.query.core.test.TestQuery;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static win.doyto.query.core.test.TestEntity.initUserEntities;

/**
 * MemoryDataAccessTest
 *
 * @author f0rb
 */
class MemoryDataAccessTest {

    MemoryDataAccess<TestEntity, Integer, TestQuery> mockUserDataAccess;

    @BeforeEach
    void setUp() {
        mockUserDataAccess = new MemoryDataAccess<>(TestEntity.class);
        initUserEntities().forEach(mockUserDataAccess::create);
    }

    @Test
    void filterByUsername() {
        TestQuery testQuery = TestQuery.builder().username("f0rb").build();
        assertEquals(1, mockUserDataAccess.query(testQuery).size());
    }

    @Test
    void filterByOr() {
        TestQuery testQuery = TestQuery.builder().usernameOrEmailOrMobile("f0rb").build();
        assertEquals(1, mockUserDataAccess.query(testQuery).size());
    }

    @Test
    void filterByLike() {
        TestQuery testQuery = TestQuery.builder().usernameLike("name").build();
        assertEquals(4, mockUserDataAccess.query(testQuery).size());
    }

    @Test
    void filterByIn() {
        List<Integer> idIn = Arrays.asList(1, 2, 3, -1);
        TestQuery testQuery = TestQuery.builder().idIn(idIn).build();
        assertEquals(3, mockUserDataAccess.query(testQuery).size());
    }

    @Test
    void filterByLt() {
        TestQuery testQuery = TestQuery.builder().idLt(3).build();
        assertEquals(2, mockUserDataAccess.query(testQuery).size());
    }

    @Test
    void filterByLe() {
        TestQuery testQuery = TestQuery.builder().idLe(3).build();
        assertEquals(3, mockUserDataAccess.query(testQuery).size());
    }

    @Test
    public void fetch() {
        TestEntity u1 = mockUserDataAccess.get(1);
        TestEntity u2 = mockUserDataAccess.get(1);
        assertSame(u1, u2);

        TestEntity f1 = mockUserDataAccess.fetch(1);
        assertNotSame(u1, f1);
    }

    @Test
    void filterByMultiConditions() {
        TestQuery testQuery = TestQuery.builder().valid(true).usernameOrEmailOrMobileLike("username").build();
        List<TestEntity> userEntities = mockUserDataAccess.query(testQuery);
        assertEquals(2, userEntities.size());
    }

    @Test
    void memoNull() {
        TestQuery byNullMemo = TestQuery.builder().build();
        assertEquals(5, mockUserDataAccess.count(byNullMemo));

        byNullMemo.setMemoNull(true);
        assertEquals(4, mockUserDataAccess.count(byNullMemo));
    }

    @Test
    void notNull() {
        TestQuery byNoneNullMemo = TestQuery.builder().memoNotNull(true).build();
        assertEquals(1, mockUserDataAccess.count(byNoneNullMemo));
    }

    @Test
    void ignoreFieldWithSubQuery() {
        TestQuery byNoneNullMemo = TestQuery.builder().roleId(1).build();
        assertEquals(5, mockUserDataAccess.count(byNoneNullMemo));
    }

    @Test
    void not() {
        TestQuery byNotNormal = TestQuery.builder().userLevelNot(TestEnum.NORMAL).build();
        assertEquals(1, mockUserDataAccess.count(byNotNormal));

        TestQuery byNotNormalAndValid = TestQuery.builder().userLevelNot(TestEnum.VIP).valid(true).build();
        assertEquals(2, mockUserDataAccess.count(byNotNormalAndValid));
    }

    @Test
    void filterByNotIn() {
        TestQuery testQuery = TestQuery.builder().idNotIn(Arrays.asList(1, 2)).build();
        assertEquals(3, mockUserDataAccess.count(testQuery));

        testQuery.setIdNotIn(Arrays.asList());
        assertEquals(5, mockUserDataAccess.count(testQuery));
    }
}