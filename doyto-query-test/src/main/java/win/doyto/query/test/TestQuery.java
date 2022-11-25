/*
 * Copyright © 2019-2022 Forb Yuan
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.annotation.QueryField;
import win.doyto.query.core.PageQuery;
import win.doyto.query.test.perm.PermissionQuery;

import java.util.Date;
import java.util.List;

/**
 * TestQuery
 *
 * @author f0rb
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TestQuery extends PageQuery {

    @DomainPath({"user", "role", "perm"})
    private PermissionQuery perm;

    private List<Integer> idIn;
    private List<Integer> idNotIn;
    private Integer idLt;
    private Integer idLe;
    private String username;
    private String usernameEq;
    private String usernameContain;

    @QueryField(and = "(username = ? OR email = ? OR mobile = ?)")
    private String account;

    private AccountOr account2;

    private String email;

    private String usernameOrEmailOrMobile;

    private String usernameOrEmailOrMobileLike;
    private String usernameLikeOrEmailLikeOrMobileLike;

    private String usernameLike;
    private String usernameStart;

    private String userNameOrUserCodeLike;

    private boolean memoNull;
    private boolean memoNotNull;

    private TestEnum userLevel;

    private TestEnum userLevelNot;
    private List<TestEnum> userLevelIn;

    private List<TestStringEnum> statusIn;

    private Date createTimeGt;
    private Date createTimeGe;
    private Date createTimeLt;
    private Date createTimeLe;

    private Boolean valid;

    // for MongoDB
    private Boolean statusExists;

}
