package win.doyto.query.cache;

/**
 * Invocable
 *
 * @author f0rb
 */
@FunctionalInterface
public interface CacheInvoker
        <T> {
    T invoke();
}
