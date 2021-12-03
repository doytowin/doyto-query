package win.doyto.query.web.demo.module.auth;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import win.doyto.query.web.demo.module.user.UserApi;
import win.doyto.query.web.demo.module.user.UserResponse;
import win.doyto.query.web.response.ErrorCode;
import win.doyto.query.web.response.JsonBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

/**
 * LoginController
 *
 * @author f0rb
 */
@Slf4j
@JsonBody
@RestController
@AllArgsConstructor
class LoginController {

    @Resource
    UserApi userApi;

    @PostMapping("login")
    public void login(@RequestBody @Valid LoginRequest loginRequest, HttpServletRequest request) {
        UserResponse userResponse = userApi.auth(loginRequest.getAccount(), loginRequest.getPassword());
        HttpSession session = request.getSession();
        session.setAttribute("user", userResponse);
        session.setAttribute("userId", userResponse.getId());
    }

    @GetMapping("account")
    public UserResponse account(HttpServletRequest request) {
        UserResponse userResponse = (UserResponse) request.getSession().getAttribute("user");
        ErrorCode.assertNotNull(userResponse, ErrorCode.build("会话过期"));
        return userResponse;
    }
}
