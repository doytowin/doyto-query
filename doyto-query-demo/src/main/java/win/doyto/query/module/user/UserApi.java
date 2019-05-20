package win.doyto.query.module.user;

/**
 * UserApi
 *
 * @author f0rb
 */
public interface UserApi {
    UserResponse auth(String loginRequest, String password);
}
