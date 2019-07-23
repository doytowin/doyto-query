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
        assertEquals(0, pageQuery.getOffset());
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
        String sort = "user_type,desc;field(user_status,2,0,11);id,asc";
        assertTrue(Pattern.compile(PageQuery.RX_SORT).matcher(sort).matches());

        assertTrue(Pattern.compile(PageQuery.RX_SORT).matcher("field(gender,'male','female');id,desc").matches());
    }
}