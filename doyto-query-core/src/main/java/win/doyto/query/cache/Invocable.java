package win.doyto.query.cache;

/**
 * Invocable
 *
 * @author f0rb
 * @date 2019-05-19
 */
@FunctionalInterface
public interface Invocable<T> {
    T invoke();
}
