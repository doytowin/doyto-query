package win.doyto.query.demo.exception;

/**
 * ServiceAsserts
 *
 * @author f0rb on 2019-06-01
 */
public class ServiceAsserts {

    public static void notNull(Object target, String message) {
        isTrue(target != null, message);
    }

    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message);
        }
    }
}
