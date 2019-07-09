package win.doyto.query.demo.exception;

/**
 * ServiceAsserts
 *
 * @author f0rb on 2019-06-01
 */
public class ServiceAsserts {

    public static <T> T notNull(T target, String message) {
        isTrue(target != null, message);
        return target;
    }

    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message);
        }
    }
}
