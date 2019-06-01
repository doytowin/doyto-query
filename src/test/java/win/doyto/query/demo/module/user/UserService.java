package win.doyto.query.demo.module.user;

import win.doyto.query.service.RestService;

/**
 * TestService
 *
 * @author f0rb
 */
public interface UserService extends RestService<UserEntity, Long, UserQuery, UserRequest, UserResponse> {

    UserResponse auth(String loginRequest, String password);

}
