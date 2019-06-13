package win.doyto.query.core;

/**
 * Invocable
 *
 * @author f0rb
 */
@FunctionalInterface
public interface Invocable<T> {
    T invoke();
}
