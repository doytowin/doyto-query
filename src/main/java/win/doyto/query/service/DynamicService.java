package win.doyto.query.service;

import win.doyto.query.core.CommonCrudService;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;

/**
 * DynamicService
 *
 * @author f0rb on 2019-05-28
 */
public interface DynamicService<E extends Persistable<I>, I extends Serializable, Q> extends CommonCrudService<E, Q> {

    /**
     * Get origin entity from sharding table
     *
     * @param param an entity just contains id and information of sharding table
     * @return origin entity
     */
    E get(E param);

    /**
     * force to get a new entity object from database
     *
     * @param param entity id
     * @return a new entity object
     */
    E fetch(E param);

    /**
     * Delete entity from sharding table
     *
     * @param param an entity just contains id and information of sharding table
     * @return origin entity
     */
    E delete(E param);

}
