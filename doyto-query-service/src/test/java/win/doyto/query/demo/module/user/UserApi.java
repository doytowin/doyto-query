package win.doyto.query.demo.module.user;

import win.doyto.query.web.controller.RestApi;

/**
 * UserApi
 *
 * @author f0rb
 */
public interface UserApi extends RestApi<Long, UserQuery, UserRequest, UserResponse> {

    UserResponse auth(String loginRequest, String password);

}
