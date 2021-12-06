package win.doyto.query.test.join;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.annotation.QueryTableAlias;
import win.doyto.query.test.TestEnum;
import win.doyto.query.test.TestPageQuery;

/**
 * TestJoinQuery
 *
 * @author f0rb on 2019-06-09
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TestJoinQuery extends TestPageQuery {

    private String roleName;

    @QueryTableAlias("u")
    private TestEnum userLevel;

    @QueryTableAlias("r")
    private String roleNameLikeOrRoleCodeLike;
}
