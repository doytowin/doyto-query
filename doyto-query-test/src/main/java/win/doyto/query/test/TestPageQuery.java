package win.doyto.query.test;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.validation.PageGroup;

import java.util.regex.Pattern;

/**
 * PageQuery
 *
 * @author f0rb
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("java:S3740")
public class TestPageQuery implements DoytoQuery {

    @SuppressWarnings("java:S5843")
    protected static final String SORT_RX = "(\\w+,(asc|desc)|field\\(\\w+(,[\\w']+)++\\))(;(\\w+,(asc|desc)|field\\(\\w+(,[\\w']+)++\\)))*";
    protected static final Pattern SORT_PTN = Pattern.compile(TestPageQuery.SORT_RX);

    private Integer pageNumber;

    private Integer pageSize;

    @javax.validation.constraints.Pattern(regexp = SORT_RX, message = "Sorting field format error", groups = PageGroup.class)
    private String sort;

    public int getPageNumber() {
        if (pageNumber == null || pageNumber < 0) {
            return 0;
        }
        return pageNumber;
    }

    public int getPageSize() {
        if (pageSize == null || pageSize < 0) {
            return 10;
        }
        return pageSize;
    }

    public int calcOffset() {
        return getPageNumber() * getPageSize();
    }

    public boolean needPaging() {
        return pageNumber != null || pageSize != null;
    }

    public void forcePaging() {
        if (!needPaging()) {
            setPageNumber(0);
        }
    }

}
