package win.doyto.query.core;

/**
 * Dialect
 *
 * @author f0rb on 2019-06-02
 */
public interface Dialect {

    /**
     * build page SQL for different
     *
     * @param sql    SQL will always be SELECT clause.
     * @param limit  LIMIT number which is GTE 0.
     * @param offset OFFSET number which is GTE 0.
     * @return pageable sql
     */
    String buildPageSql(String sql, int limit, long offset);

    default String wrapLabel(String fieldName) {
        return fieldName;
    }
}
