package win.doyto.query.entity;

/**
 * EntityAspect
 *
 * @author f0rb
 */
public interface EntityAspect<E> {

    default void afterCreate(E e) {
    }

    default void afterUpdate(E origin, E current) {
    }

    default void afterDelete(E e) {
    }

}
