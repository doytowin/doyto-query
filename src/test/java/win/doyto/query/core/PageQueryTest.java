package win.doyto.query.core;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PageQueryTest
 *
 * @author f0rb
 */
class PageQueryTest {

    @Test
    void needPaging() {
        PageQuery pageQuery = new PageQuery();
        assertFalse(pageQuery.needPaging());
        assertEquals(0, pageQuery.calcOffset());
    }

    @Test
    void needPagingAfterSetPageNumber() {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNumber(1);
        assertTrue(pageQuery.needPaging());
        assertEquals(1, (int) pageQuery.getPageNumber());
        assertEquals(10, (int) pageQuery.getPageSize());
    }

    @Test
    void needPagingAfterSetPageSize() {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageSize(10);
        assertTrue(pageQuery.needPaging());
        assertEquals(0, (int) pageQuery.getPageNumber());
        assertEquals(10, (int) pageQuery.getPageSize());
    }

    @Test
    void regex() {
        Pattern sortPtn = Pattern.compile(PageQuery.RX_SORT);

        assertTrue(sortPtn.matcher("user_type,desc;field(user_status,2,0,11);id,asc").matches());

        assertTrue(sortPtn.matcher("field(gender,'male','female');id,desc").matches());

        assertTrue(sortPtn.matcher("field(gender,'male','female')").matches());
    }
}