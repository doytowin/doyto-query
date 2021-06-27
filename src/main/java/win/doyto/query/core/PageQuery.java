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
import javax.validation.constraints.Pattern;

import static java.lang.Math.max;

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

    protected static final String SORT_RX = "(\\w+,(asc|desc);|field\\(\\w+(,[\\w']+)++\\);)*(\\w+,(asc|desc)|field\\(\\w+(,[\\w']+)++\\))";
    protected static final java.util.regex.Pattern SORT_PTN = java.util.regex.Pattern.compile(PageQuery.SORT_RX);

    private Integer pageNumber;

    private Integer pageSize;

    @ApiModelProperty(value = "Sorting field, format: field1,desc;field2,asc;field(col,'v1','v2')")
    @Pattern(regexp = SORT_RX, message = "Sorting field format error", groups = PageGroup.class)
    private String sort;

    public Integer getPageNumber() {
        return getDefault(pageNumber, 0, pageSize == null);
    }

    public Integer getPageSize() {
        return getDefault(pageSize, 10, pageNumber == null);
    }

    private Integer getDefault(Integer number, int defaultValue, boolean canBeNull) {
        if (number == null) {
            if (canBeNull) {
                return null;
            } else {
                number = defaultValue;
            }
        }
        return max(0, number);
    }

    public int calcOffset() {
        Integer page = getPageNumber();
        return page == null ? 0 : GlobalConfiguration.adjustStartPageNumber(page) * getPageSize();
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
