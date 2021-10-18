package win.doyto.query.core;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import win.doyto.query.config.GlobalConfiguration;
import win.doyto.query.validation.PageGroup;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * PageQuery
 *
 * @author f0rb
 */
@Getter
@Setter
@Accessors(chain = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("java:S3740")
public class PageQuery implements Serializable {

    @SuppressWarnings("java:S5843")
    protected static final String SORT_RX = "(\\w+,(asc|desc)|field\\(\\w+(,[\\w']+)++\\))(;(\\w+,(asc|desc)|field\\(\\w+(,[\\w']+)++\\)))*";
    protected static final Pattern SORT_PTN = Pattern.compile(PageQuery.SORT_RX);

    private Integer pageNumber;

    private Integer pageSize;

    @ApiModelProperty(value = "Sorting field, format: field1,desc;field2,asc;field(col,'v1','v2')")
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
        return GlobalConfiguration.adjustStartPageNumber(getPageNumber()) * getPageSize();
    }

    public boolean needPaging() {
        return pageNumber != null || pageSize != null;
    }

    public void forcePaging() {
        if (!needPaging()) {
            setPageNumber(0);
        }
    }

    protected IdWrapper toIdWrapper() {
        return IdWrapper.build(null);
    }
}
