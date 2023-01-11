/*
 * Copyright © 2019-2023 Forb Yuan
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

package win.doyto.query.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import win.doyto.query.entity.AbstractPersistable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * TestEntity
 *
 * @author f0rb 2019-05-12
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = TestEntity.TABLE)
public class TestEntity extends AbstractPersistable<Integer> {
    public static final String TABLE = "t_user";
    @Column
    private String username;
    private String password;
    private String mobile;
    private String email;
    private String nickname;
    private TestEnum userLevel;
    private String memo;
    private Integer score;
    private Boolean valid;

    @Transient
    private Date createTime;

    private static final int INIT_SIZE = 5;

    public static List<TestEntity> initUserEntities() {
        List<TestEntity> userEntities = new ArrayList<>(INIT_SIZE);

        for (int i = 1; i < INIT_SIZE; i++) {
            TestEntity testEntity = new TestEntity();
            testEntity.setId(i);
            testEntity.setUsername("username" + i);
            testEntity.setPassword("password" + i);
            testEntity.setEmail("test" + i + "@163.com");
            testEntity.setMobile("1777888888" + i);
            testEntity.setUserLevel(TestEnum.NORMAL);
            testEntity.setValid(i % 2 == 0);
            testEntity.setScore(i * 10);
            userEntities.add(testEntity);
        }
        TestEntity testEntity = new TestEntity();
        testEntity.setId(INIT_SIZE);
        testEntity.setUsername("f0rb");
        testEntity.setNickname("自在");
        testEntity.setPassword("123456");
        testEntity.setEmail("f0rb@163.com");
        testEntity.setMobile("17778888880");
        testEntity.setUserLevel(TestEnum.VIP);
        testEntity.setValid(true);
        testEntity.setMemo("master");
        testEntity.setScore(100);
        userEntities.add(testEntity);
        return userEntities;
    }
}
