package win.doyto.query.module.auth;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import win.doyto.query.module.user.UserControllerTest;
import win.doyto.query.module.user.UserResponse;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * LoginControllerTest
 *
 * @author f0rb
 */
@Slf4j
class LoginControllerTest {

    LoginController loginController;
    HttpServletRequest httpRequest;

    @BeforeEach
    void setUp() {
        loginController = new LoginController(UserControllerTest.userController);
        httpRequest = new MockHttpServletRequest();
        loginController.userApi = UserControllerTest.userController;
    }

    @Test
    void login() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setAccount("f0rb");
        loginRequest.setPassword("123456");
        try {
            loginController.login(loginRequest, httpRequest);
        } catch (Exception e) {
            fail("Login failed", e);
        }

        UserResponse userResponse = loginController.account(httpRequest);
        assertEquals("自在", userResponse.getNickname());

    }

    @Test
    void notLogin() {
        try {
            loginController.account(httpRequest);
        } catch (Exception e) {
            assertEquals("会话过期", e.getMessage());
        }
    }

    @Test
    void loginWithWrongAccount() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setAccount("none");
        loginRequest.setPassword("123456");
        try {
            loginController.login(loginRequest, httpRequest);
            fail("Login with wrong account should fail");
        } catch (Exception e) {
            assertEquals("账号不存在", e.getMessage());
        }
    }

    @Test
    void loginWithWrongPassword() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setAccount("f0rb@163.com");
        loginRequest.setPassword("12345678");
        try {
            loginController.login(loginRequest, httpRequest);
            fail("Login with wrong password should fail");
        } catch (Exception e) {
            assertEquals("密码错误", e.getMessage());
        }
    }

    @Test
    void loginWithWrongInvalid() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setAccount("17778888881");
        loginRequest.setPassword("123456");
        try {
            loginController.login(loginRequest, httpRequest);
            fail("Login with wrong password should fail");
        } catch (Exception e) {
            assertEquals("账号被禁用", e.getMessage());
        }
    }
}