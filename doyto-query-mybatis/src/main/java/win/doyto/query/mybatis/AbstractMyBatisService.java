package win.doyto.query.mybatis;

import win.doyto.query.core.AbstractCrudService;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;

/**
 * AbstractMyBatisService
 *
 * @author f0rb
 */
public abstract class AbstractMyBatisService<E extends Persistable<I>, I extends Serializable, Q> extends AbstractCrudService<E, I, Q> {

    public AbstractMyBatisService(CrudMapper<E, I, Q> queryMapper) {
        super(queryMapper);
    }

}
