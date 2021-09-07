package win.doyto.query.demo.common;

import org.springframework.stereotype.Component;
import win.doyto.query.entity.UserIdProvider;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * SessionUserIdProvider
 *
 * @author f0rb
 */
@Component
public class SessionUserIdProvider implements UserIdProvider<Long> {
    @Resource
    private HttpServletRequest request;

    @Override
    public Long getUserId() {
        return (Long) request.getSession().getAttribute("userId");
    }
}
