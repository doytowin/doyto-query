package win.doyto.query.common;

import org.springframework.stereotype.Component;
import win.doyto.query.entity.UserIdProvider;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * SessionUserIdProvider
 *
 * @author f0rb
 * @date 2019-05-18
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
