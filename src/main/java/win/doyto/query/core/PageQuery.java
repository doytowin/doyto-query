package win.doyto.query.core;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import win.doyto.query.validation.PageGroup;

import java.io.Serializable;
import javax.validation.constraints.Pattern;

import static java.lang.Math.max;

/**
 * PageQuery
 *
 * @author f0rb
 */
@Accessors(chain = true)
public class PageQuery implements Serializable {

    static final String RX_SORT = "(\\w+,(asc|desc);|field\\(\\w+(,[\\w']+)+\\);)*\\w+,(asc|desc)";

    @Setter
    private Integer pageNumber;

    @Setter
    private Integer pageSize;

    @ApiModelProperty(value = "Sorting field, format: field1,desc;field2,asc")
    @Pattern(regexp = RX_SORT, message = "Sorting field format error", groups = PageGroup.class)
    @Getter
    @Setter
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
        return needPaging() ? getPageSize() * getPageNumber() : 0;
    }

    public boolean needPaging() {
        return getPageNumber() != null;
    }
}
