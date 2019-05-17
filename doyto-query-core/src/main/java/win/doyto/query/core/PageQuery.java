package win.doyto.query.core;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.Pattern;

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
        return getDefault(pageNumber, 0);
    }

    public Integer getPageSize() {
        return getDefault(pageSize, 10);
    }

    private Integer getDefault(Integer pageSize, int i) {
        if (pageSize == null) {
            return null;
        } else {
            return pageSize < 0 ? i : pageSize;
        }
    }

    public int getOffset() {
        return getPageSize() * getPageNumber();
    }

    public boolean needPaging() {
        return getPageNumber() != null && getPageSize() != null;
    }
}
