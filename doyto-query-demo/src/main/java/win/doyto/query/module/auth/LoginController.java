package win.doyto.query.module.auth;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import win.doyto.query.exception.ServiceException;
import win.doyto.query.module.user.UserApi;
import win.doyto.query.module.user.UserResponse;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * LoginController
 *
 * @author f0rb
 */
@Slf4j
@RestController
@AllArgsConstructor
@SuppressWarnings("squid:S4529")
class LoginController {

    @Resource
    UserApi userApi;

    @PostMapping("login")
    public void login(@RequestBody @Valid LoginRequest loginRequest, HttpServletRequest request) {
        UserResponse userResponse = userApi.auth(loginRequest.getAccount(), loginRequest.getPassword());
        request.getSession().setAttribute("user", userResponse);
        request.getSession().setAttribute("userId", userResponse.getId());
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
