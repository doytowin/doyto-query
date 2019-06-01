package win.doyto.query.core.test;

import win.doyto.query.service.AbstractCrudService;

/**
 * TestService
 *
 * @author f0rb
 */
public class TestService extends AbstractCrudService<TestEntity, Integer, TestQuery> {
    @Override
    protected String getCacheName() {
        return "module:user";
    }
}
