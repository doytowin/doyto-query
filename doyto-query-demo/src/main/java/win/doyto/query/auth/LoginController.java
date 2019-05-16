package win.doyto.query.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import win.doyto.query.exception.ServiceException;
import win.doyto.query.user.UserApi;
import win.doyto.query.user.UserResponse;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * LoginController
 *
 * @author f0rb
 * @date 2019-05-16
 */
@Slf4j
@RestController
public class LoginController {

    @Resource
    UserApi userApi;

    @PostMapping("login")
    public void login(@RequestBody @Valid LoginRequest loginRequest, HttpServletRequest request) {
        UserResponse userResponse = userApi.auth(loginRequest.getAccount(), loginRequest.getPassword());
        request.getSession().setAttribute("user", userResponse);
    }

    @GetMapping("account")
    public UserResponse account(HttpServletRequest request) {
        UserResponse userResponse = (UserResponse) request.getSession().getAttribute("user");
        if (userResponse == null) {
            throw new ServiceException("会话过期");
        }
        return userResponse;
    }
}
