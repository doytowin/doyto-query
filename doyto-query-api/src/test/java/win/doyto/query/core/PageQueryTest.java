/*
 * Copyright © 2019-2025 DoytoWin, Inc.
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
    }

    @Test
    void needPagingAfterSetPageNumber() {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPage(1);
        assertTrue(pageQuery.needPaging());
        assertEquals(1, pageQuery.getPageNumber());
        assertEquals(10, pageQuery.getPageSize());
    }

    @Test
    void needPagingAfterSetPageSize() {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setSize(10);
        assertTrue(pageQuery.needPaging());
        assertEquals(0, pageQuery.getPageNumber());
        assertEquals(10, pageQuery.getPageSize());
    }

    @Test
    void regex() {
        Pattern sortPtn = PageQuery.SORT_PTN;

        assertTrue(sortPtn.matcher("user_type,desc;field(user_status,2,0,11);id,asc").matches());

        assertTrue(sortPtn.matcher("field(gender,'male','female');id,desc").matches());

        assertTrue(sortPtn.matcher("field(gender,'male','female')").matches());
    }

}