package win.doyto.query.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.core.test.TestEntity;
import win.doyto.query.core.test.TestEnum;
import win.doyto.query.core.test.TestQuery;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static win.doyto.query.core.test.TestEntity.initUserEntities;

/**
 * MemoryDataAccessTest
 *
 * @author f0rb
 */
class MemoryDataAccessTest {

    MemoryDataAccess<TestEntity, Integer, TestQuery> testMemoryDataAccess;

    @BeforeEach
    void setUp() {
        testMemoryDataAccess = new MemoryDataAccess<>(TestEntity.class);
        testMemoryDataAccess.batchInsert(initUserEntities());
    }

    @Test
    void filterByUsername() {
        TestQuery testQuery = TestQuery.builder().username("f0rb").build();
        assertEquals(1, testMemoryDataAccess.query(testQuery).size());
    }

    @Test
    void filterByOr() {
        TestQuery testQuery = TestQuery.builder().usernameOrEmailOrMobile("f0rb").build();
        assertEquals(1, testMemoryDataAccess.query(testQuery).size());
    }

    @Test
    void filterByOrFirst() {
        GlobalConfiguration.instance().setSplitOrFirst(false);
        TestQuery testQuery = TestQuery.builder().usernameOrEmailOrMobileLike("username").build();
        assertEquals(4, testMemoryDataAccess.query(testQuery).size());
        GlobalConfiguration.instance().setSplitOrFirst(true);
        assertEquals(0, testMemoryDataAccess.query(testQuery).size());

        testQuery.setUsernameOrEmailOrMobileLike("1777888888");
        assertEquals(5, testMemoryDataAccess.query(testQuery).size());
    }

    @Test
    void filterByLike() {
        TestQuery testQuery = TestQuery.builder().usernameLike("name").build();
        assertEquals(4, testMemoryDataAccess.query(testQuery).size());
    }

    @Test
    void filterByIn() {
        List<Integer> idIn = Arrays.asList(1, 2, 3, -1);
        TestQuery testQuery = TestQuery.builder().idIn(idIn).build();
        assertEquals(3, testMemoryDataAccess.query(testQuery).size());
    }

    @Test
    void filterByLt() {
        TestQuery testQuery = TestQuery.builder().idLt(3).build();
        assertEquals(2, testMemoryDataAccess.query(testQuery).size());
    }

    @Test
    void filterByLe() {
        TestQuery testQuery = TestQuery.builder().idLe(3).build();
        assertEquals(3, testMemoryDataAccess.query(testQuery).size());
    }

    @Test
    public void getShouldReturnDifferentEntityObject() {
        TestEntity u1 = testMemoryDataAccess.get(1);
        TestEntity u2 = testMemoryDataAccess.get(1);
        assertNotSame(u1, u2);
    }

    @Test
    void filterByMultiConditions() {
        TestQuery testQuery = TestQuery.builder().valid(true).usernameLikeOrEmailLikeOrMobileLike("username").build();
        List<TestEntity> userEntities = testMemoryDataAccess.query(testQuery);
        assertEquals(2, userEntities.size());
    }

    @Test
    void memoNull() {
        TestQuery byNullMemo = TestQuery.builder().build();
        assertEquals(5, testMemoryDataAccess.count(byNullMemo));

        byNullMemo.setMemoNull(true);
        assertEquals(4, testMemoryDataAccess.count(byNullMemo));
    }

    @Test
    void notNull() {
        TestQuery byNoneNullMemo = TestQuery.builder().memoNotNull(true).build();
        assertEquals(1, testMemoryDataAccess.count(byNoneNullMemo));
    }

    @Test
    void ignoreFieldWithSubQuery() {
        TestQuery byNoneNullMemo = TestQuery.builder().roleId(1).build();
        assertEquals(5, testMemoryDataAccess.count(byNoneNullMemo));
    }

    @Test
    void not() {
        TestQuery byNotNormal = TestQuery.builder().userLevelNot(TestEnum.NORMAL).build();
        assertEquals(1, testMemoryDataAccess.count(byNotNormal));

        TestQuery byNotNormalAndValid = TestQuery.builder().userLevelNot(TestEnum.VIP).valid(true).build();
        assertEquals(2, testMemoryDataAccess.count(byNotNormalAndValid));
    }

    @Test
    void filterByNotIn() {
        TestQuery testQuery = TestQuery.builder().idNotIn(Arrays.asList(1, 2)).build();
        assertEquals(3, testMemoryDataAccess.count(testQuery));

        testQuery.setIdNotIn(Arrays.asList());
        assertEquals(5, testMemoryDataAccess.count(testQuery));
    }

    @Test
    void filterByStart() {
        TestQuery byUsernameLike = TestQuery.builder().usernameLike("name").build();
        assertEquals(4, testMemoryDataAccess.count(byUsernameLike));

        TestQuery byUsernameStart = TestQuery.builder().usernameStart("name").build();
        assertEquals(0, testMemoryDataAccess.count(byUsernameStart));
    }

    @Test
    void patch() {
        TestEntity testEntity = new TestEntity();
        testEntity.setMemo("invalid");
        TestQuery byNotValid = TestQuery.builder().valid(false).build();
        testMemoryDataAccess.patch(testEntity, byNotValid);

        assertThat(testMemoryDataAccess.query(byNotValid)).extracting(TestEntity::getMemo).containsExactly("invalid", "invalid");
    }

    @Test
    void sort() {
        TestQuery sort = TestQuery.builder().build();
        sort.setSort("id,desc");
        assertThat(testMemoryDataAccess.query(sort)).extracting(TestEntity::getId).containsExactly(5, 4, 3, 2, 1);

        sort.setSort("valid,asc;id,desc");
        assertThat(testMemoryDataAccess.query(sort)).extracting(TestEntity::getId).containsExactly(3, 1, 5, 4, 2);

        sort.setValid(true);
        assertThat(testMemoryDataAccess.query(sort)).extracting(TestEntity::getId).containsExactly(5, 4, 2);
    }
}