/*
 * Copyright © 2019-2024 Forb Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.doyto.query.jdbc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcOperations;
import win.doyto.query.core.AssociationService;
import win.doyto.query.core.UniqueKey;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AssociationServiceTest
 *
 * @author f0rb on 2021-12-31
 */
class AssociationServiceTest extends JdbcApplicationTest {

    @Autowired
    @Qualifier("userAndRoleAssociationService")
    private AssociationService<Long, Integer> userAndRoleAssociationService;

    @Test
    void associate() {
        int ret = userAndRoleAssociationService.associate(1L, 20);
        assertThat(ret).isEqualTo(1);
    }

    @Test
    void queryK1ByK2() {
        List<Long> userIds = userAndRoleAssociationService.queryK1ByK2(2);
        assertThat(userIds).containsExactly(1L, 4L);
    }

    @Test
    void queryK2ByK1() {
        List<Integer> roleIds = userAndRoleAssociationService.queryK2ByK1(1L);
        assertThat(roleIds).containsExactly(1, 2);
    }

    @Test
    void deleteByK1() {
        int ret = userAndRoleAssociationService.deleteByK1(1L);
        assertThat(ret).isEqualTo(2);
    }

    @Test
    void deleteByK2() {
        int ret = userAndRoleAssociationService.deleteByK2(1);
        assertThat(ret).isEqualTo(3);
    }

    @Test
    void reassociateForK1() {
        int ret = userAndRoleAssociationService.reassociateForK1(1L, Arrays.asList(2, 3));
        assertThat(ret).isEqualTo(2);
        assertThat(userAndRoleAssociationService.queryK2ByK1(1L)).containsExactly(2, 3);
    }

    @Test
    void reassociateForK1WithEmptyK2() {
        int ret = userAndRoleAssociationService.reassociateForK1(1L, Arrays.asList());
        assertThat(ret).isZero();
        assertThat(userAndRoleAssociationService.queryK2ByK1(1L)).isEmpty();
    }

    @Test
    void reassociateForK2() {
        List<Long> k1List = Arrays.asList(1L, 2L, 3L, 4L);
        int ret = userAndRoleAssociationService.reassociateForK2(1, k1List);
        assertThat(ret).isEqualTo(4);
        assertThat(userAndRoleAssociationService.queryK1ByK2(1)).hasSameElementsAs(k1List);
    }

    @Test
    void reassociateForK2WithEmptyK1() {
        int ret = userAndRoleAssociationService.reassociateForK2(1, Arrays.asList());
        assertThat(ret).isZero();
        assertThat(userAndRoleAssociationService.queryK1ByK2(1)).isEmpty();
    }

    @Test
    void count() {
        Set<UniqueKey<Long, Integer>> ukSet = new HashSet<>();
        ukSet.add(new UniqueKey<>(1L, 2));
        ukSet.add(new UniqueKey<>(1L, 3));
        ukSet.add(new UniqueKey<>(1L, 4));

        long ret = userAndRoleAssociationService.count(ukSet);
        assertThat(ret).isEqualTo(1);
    }

    @Test
    void dissociate() {
        long ret = userAndRoleAssociationService.dissociate(1L, 2);
        assertThat(ret).isEqualTo(1);

        ret = userAndRoleAssociationService.dissociate(1L, 2);
        assertThat(ret).isZero();
    }

    @Test
    void buildUniqueKeysAsSet() {
        Collection<UniqueKey<Long, Integer>> ukSet = userAndRoleAssociationService.buildUniqueKeys(1L, Arrays.asList(2, 2, 3, 4));
        assertThat(ukSet).containsExactly(new UniqueKey<>(1L, 2), new UniqueKey<>(1L, 3), new UniqueKey<>(1L, 4));

        Collection<UniqueKey<Long, Integer>> ukSet2 = userAndRoleAssociationService.buildUniqueKeys(Arrays.asList(2L, 2L, 3L, 4L), 1);
        assertThat(ukSet2).containsOnlyOnce(new UniqueKey<>(2L, 1), new UniqueKey<>(3L, 1), new UniqueKey<>(4L, 1));
    }

    @Test
    void exists() {
        assertThat(userAndRoleAssociationService.exists(1L, 2)).isTrue();
        assertThat(userAndRoleAssociationService.exists(1L, 5)).isFalse();

        Set<UniqueKey<Long, Integer>> uniqueKeys = userAndRoleAssociationService.buildUniqueKeys(1L, Arrays.asList(2, 5));
        assertThat(userAndRoleAssociationService.exists(uniqueKeys)).isTrue();
    }

    @Test
    void existsExactly() {
        Set<UniqueKey<Long, Integer>> uniqueKeys = userAndRoleAssociationService.buildUniqueKeys(1L, Arrays.asList(2, 5));
        assertThat(userAndRoleAssociationService.existsExactly(uniqueKeys)).isFalse();
    }

    @Test
    void associateWithCreateUserId(@Autowired JdbcOperations jdbcOperations) {
        userAndRoleAssociationService.associate(5L, 20);

        String sql = "select count(*) from a_user_and_role where create_user_id = 0";
        Long ret = jdbcOperations.queryForObject(sql, Long.class);
        assertThat(ret).isEqualTo(1L);
    }

    @Test
    void associateWithExistedId() {
        Set<UniqueKey<Long, Integer>> uniqueKeys = userAndRoleAssociationService.buildUniqueKeys(1L, Arrays.asList(1, 2, 3));
        int ret = userAndRoleAssociationService.associate(uniqueKeys);
        assertThat(ret).isEqualTo(1);
    }

}