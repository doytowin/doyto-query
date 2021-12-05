package win.doyto.query.core;

import java.io.Serializable;

/**
 * Pageable
 *
 * @author f0rb on 2021-12-02
 */
public interface Pageable extends Serializable {
    void forcePaging();

    String getSort();

    boolean needPaging();

    int getPageSize();

    int calcOffset();

    @SuppressWarnings("java:S3740")
    default IdWrapper toIdWrapper() {
        return IdWrapper.build(null);
    }
}

