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

package win.doyto.query.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.validation.PageGroup;

import java.util.regex.Pattern;
import javax.persistence.Transient;

/**
 * PageQuery
 *
 * @author f0rb
 * @since 0.0.1
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("java:S3740")
public class PageQuery implements DoytoQuery {

    @SuppressWarnings("java:S5843")
    protected static final String SORT_RX = "(\\w+,(asc|desc)|field\\(\\w+(,[\\w']+)++\\))(;(\\w+,(asc|desc)|field\\(\\w+(,[\\w']+)++\\)))*";
    protected static final Pattern SORT_PTN = Pattern.compile(PageQuery.SORT_RX);

    @Transient
    private Integer pageNumber;

    @Transient
    private Integer pageSize;

    /**
     * Sorting field, format: field1,desc;field2,asc;field(col,'v1','v2')
     */
    @Transient
    @javax.validation.constraints.Pattern(regexp = SORT_RX, message = "Sorting field format error", groups = PageGroup.class)
    private String sort;

    public int getPageNumber() {
        if (pageNumber == null || pageNumber < 0) {
            return 0;
        }
        return pageNumber;
    }

    @Override
    public int getPageSize() {
        if (pageSize == null || pageSize < 0) {
            return 10;
        }
        return pageSize;
    }

    @Override
    public boolean needPaging() {
        return pageNumber != null || pageSize != null;
    }

    @Override
    public void forcePaging() {
        if (!needPaging()) {
            setPageNumber(0);
        }
    }

}
