package win.doyto.query.core;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.Pattern;

import static java.lang.Math.max;

/**
 * PageQuery
 *
 * @author f0rb
 * @date 2019-05-12
 */
@Setter
@Accessors(chain = true)
public class PageQuery {

    private Integer pageNumber;

    private Integer pageSize;

    @ApiModelProperty(value = "Sorting field, format: field1,desc;field2,asc")
    @Pattern(regexp = "([_\\w]+,(asc|desc);)*[_\\w]+,(asc|desc)", message = "Sorting field format error")
    @Getter
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

    public int getOffset() {
        return needPaging() ? getPageSize() * getPageNumber() : 0;
    }

    public boolean needPaging() {
        return getPageNumber() != null;
    }
}
