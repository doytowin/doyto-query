package win.doyto.query.core;

import org.junit.jupiter.api.Test;
import win.doyto.query.core.test.TestQuery;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * BuildHelperTest
 *
 * @author f0rb on 2021-02-16
 */
class BuildHelperTest {

    @Test
    void buildOrderByForFieldSorting() {
        PageQuery pageQuery = TestQuery.builder().build().setSort("FIELD(status,1,3,2,0);id,DESC");
        assertEquals(" ORDER BY FIELD(status,1,3,2,0), id DESC", BuildHelper.buildOrderBy("", pageQuery));

        pageQuery.setSort(OrderByBuilder.create().field("gender", "'male'", "'female'").desc("id").toString());
        assertEquals(" ORDER BY field(gender,'male','female'), id desc", BuildHelper.buildOrderBy("", pageQuery));
    }

}