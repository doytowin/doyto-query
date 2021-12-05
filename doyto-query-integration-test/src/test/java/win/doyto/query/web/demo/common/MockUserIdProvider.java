package win.doyto.query.web.demo.common;

import org.springframework.stereotype.Component;
import win.doyto.query.entity.UserIdProvider;

/**
 * MockUserIdProvider
 *
 * @author f0rb on 2020-04-20
 */
@Component
public class MockUserIdProvider implements UserIdProvider<Long> {
    @Override
    public Long getUserId() {
        return 1L;
    }

}
