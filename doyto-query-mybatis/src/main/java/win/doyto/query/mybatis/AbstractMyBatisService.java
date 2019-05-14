package win.doyto.query.mybatis;

import win.doyto.query.core.AbstractService;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;

/**
 * AbstractMyBatisService
 *
 * @author f0rb
 * @date 2019-05-14
 */
public abstract class AbstractMyBatisService<E extends Persistable<I>, I extends Serializable, Q> extends AbstractService<E, I, Q> {

    public AbstractMyBatisService(CrudMapper<E, I, Q> queryMapper) {
        super(queryMapper);
    }

}
