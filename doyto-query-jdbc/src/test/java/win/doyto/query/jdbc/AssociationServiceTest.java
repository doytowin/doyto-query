/*
 * Copyright © 2019-2021 Forb Yuan
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
import org.springframework.jdbc.core.JdbcOperations;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AssociationServiceTest
 *
 * @author f0rb on 2021-12-31
 */
class AssociationServiceTest extends JdbcApplicationTest{
    private AssociationService<Long, Integer> associationService;

    public AssociationServiceTest(@Autowired JdbcOperations jdbcOperations) {
        this.associationService = new JdbcAssociationService<>(jdbcOperations, "t_user_and_role", "userId","roleId");
    }

    @Test
    void associate() {
        int ret = associationService.associate(1L, 20);
        assertThat(ret).isEqualTo(1);
    }

    @Test
    void queryK1ByK2() {
        List<Long> userIds = associationService.queryK1ByK2(2);
        assertThat(userIds).containsExactly(1L, 4L);
    }

    @Test
    void queryK2ByK1() {
        List<Integer> roleIds = associationService.queryK2ByK1(1L);
        assertThat(roleIds).containsExactly(1, 2);
    }

    @Test
    void deleteByK1() {
        int ret = associationService.deleteByK1(1L);
        assertThat(ret).isEqualTo(2);
    }

    @Test
    void deleteByK2() {
        int ret = associationService.deleteByK2(1);
        assertThat(ret).isEqualTo(3);
    }

    @Test
    void reassociateForK1() {
        int ret = associationService.reassociateForK1(1L, Arrays.asList(2, 3));
        assertThat(ret).isEqualTo(2);
        assertThat(associationService.queryK2ByK1(1L)).containsExactly(2, 3);
    }

    @Test
    void reassociateForK1WithEmptyK2() {
        int ret = associationService.reassociateForK1(1L, Arrays.asList());
        assertThat(ret).isZero();
        assertThat(associationService.queryK2ByK1(1L)).isEmpty();
    }

    @Test
    void reassociateForK2() {
        List<Long> k1List = Arrays.asList(1L, 2L, 3L, 4L);
        int ret = associationService.reassociateForK2(1, k1List);
        assertThat(ret).isEqualTo(4);
        assertThat(associationService.queryK1ByK2(1)).hasSameElementsAs(k1List);
    }
}