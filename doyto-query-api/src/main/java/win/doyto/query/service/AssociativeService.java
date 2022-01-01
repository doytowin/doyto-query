package win.doyto.query.service;

import java.util.Collection;
import java.util.List;

import static java.util.Collections.singleton;

/**
 * AssociativeService
 *
 * @author f0rb on 2019-05-30
 * @deprecated use {@link AssociationService}
 */
@SuppressWarnings("java:S1133")
@Deprecated
public interface AssociativeService<L, R> {

    List<R> getByLeftId(L leftId);

    int deleteByLeftId(L leftId);

    List<L> getByRightId(R rightId);

    int deleteByRightId(R rightId);

    boolean exists(Collection<L> leftIds, Collection<R> rightIds);

    default boolean exists(L leftId, R rightId) {
        return exists(leftId, singleton(rightId));
    }

    default boolean exists(L leftId, Collection<R> rightIds) {
        return exists(singleton(leftId), rightIds);
    }

    default boolean exists(Collection<L> leftIds, R rightId) {
        return exists(leftIds, singleton(rightId));
    }

    long count(Collection<L> leftIds, Collection<R> rightIds);

    default int allocate(L leftId, R rightId) {
        return !exists(leftId, rightId) ? allocate(singleton(leftId), singleton(rightId)) : 0;
    }

    int allocate(Collection<L> leftIds, Collection<R> rightIds);

    default void deallocate(L leftId, R rightId) {
        deallocate(singleton(leftId), singleton(rightId));
    }

    int deallocate(Collection<L> leftIds, Collection<R> rightIds);

    int reallocateForLeft(L leftId, Collection<R> rightIds);

    int reallocateForRight(R rightId, Collection<L> leftIds);

}
