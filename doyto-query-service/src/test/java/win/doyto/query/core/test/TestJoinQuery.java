package win.doyto.query.core.test;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.QueryTableAlias;
import win.doyto.query.core.PageQuery;

/**
 * TestJoinQuery
 *
 * @author f0rb on 2019-06-09
 */
@Getter
@Setter
public class TestJoinQuery extends PageQuery {

    private String roleName;

    @QueryTableAlias("u")
    private TestEnum userLevel;

    @QueryTableAlias("r")
    private String roleNameLikeOrRoleCodeLike;
}
