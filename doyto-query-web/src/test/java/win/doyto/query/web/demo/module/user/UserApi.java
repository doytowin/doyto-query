package win.doyto.query.web.demo.module.user;

import win.doyto.query.web.controller.RestApi;

/**
 * UserApi
 *
 * @author f0rb on 2021-07-16
 */
public interface UserApi extends RestApi<Long, UserQuery, UserRequest, UserResponse> {
    UserResponse auth(String loginRequest, String password);
}
