package win.doyto.query.core;

import java.io.Serializable;

/**
 * Pageable
 *
 * @author f0rb on 2021-12-02
 */
public interface Pageable extends Serializable {
    void forcePaging();

    boolean needPaging();

    void setPageSize(Integer size);

    int getPageSize();

    void setPageNumber(Integer page);

    int getPageNumber();

    void setSort(String sort);

    String getSort();

    int calcOffset();

    @SuppressWarnings("java:S3740")
    default IdWrapper toIdWrapper() {
        return IdWrapper.build(null);
    }
}

