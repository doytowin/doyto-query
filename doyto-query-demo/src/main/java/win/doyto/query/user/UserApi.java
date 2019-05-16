package win.doyto.query.user;

/**
 * UserApi
 *
 * @author f0rb
 * @date 2019-05-16
 */
public interface UserApi {
    UserResponse auth(String loginRequest, String password);
}
