package win.doyto.query.core;

/**
 * Dialect
 *
 * @author f0rb on 2019-06-02
 */
public interface Dialect {
    String buildPageSql(String sql, int limit, long offset);

    default String wrapLabel(String fieldName) {
        return fieldName;
    }
}
