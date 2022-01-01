package win.doyto.query.sql;

/**
 * Constant
 *
 * @author f0rb on 2019-06-03
 */
@SuppressWarnings("java:S1214")
interface Constant {
    String SEPARATOR = ", ";
    String PLACE_HOLDER = "?";
    String SPACE = " ";
    String EQUAL = " = ";
    String SELECT = "SELECT ";
    String COUNT = "count(*)";
    String FROM = " FROM ";
    String WHERE = " WHERE ";
    String EMPTY = "";
    String SPACE_OR = " OR ";
    String DELETE_FROM = "DELETE" + FROM;
}
