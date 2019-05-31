package win.doyto.query.cache;

/**
 * Invocable
 *
 * @author f0rb
 */
@FunctionalInterface
public interface Invocable<T> {
    T invoke();
}
