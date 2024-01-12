/*
 * Copyright © 2019-2024 Forb Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.doyto.query.web.demo.module.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import win.doyto.query.web.demo.module.user.UserData;
import win.doyto.query.web.demo.module.user.UserResponse;

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
        loginController = new LoginController(UserData.getUserController());
        httpRequest = new MockHttpServletRequest();
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