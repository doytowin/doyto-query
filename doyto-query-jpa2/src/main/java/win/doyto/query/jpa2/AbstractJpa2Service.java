package win.doyto.query.jpa2;

import org.springframework.data.repository.CrudRepository;
import win.doyto.query.core.AbstractCrudService;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import javax.annotation.Resource;
import javax.persistence.EntityManager;

/**
 * AbstractJpa2Service
 *
 * @author f0rb
 */
public class AbstractJpa2Service<E extends Persistable<I>, I extends Serializable, Q> extends AbstractCrudService<E, I, Q> {

    public AbstractJpa2Service(CrudRepository<E, I> crudRepository) {
        super(new Jpa2DataAccess<>(crudRepository));
    }

    @Resource
    public void setEntityManager(EntityManager entityManager) {
        ((Jpa2DataAccess) dataAccess).setEntityManager(entityManager);
    }

}
