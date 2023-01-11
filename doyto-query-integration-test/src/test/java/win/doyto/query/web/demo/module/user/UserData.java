/*
 * Copyright © 2019-2023 Forb Yuan
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

package win.doyto.query.web.demo.module.user;

import java.util.ArrayList;
import java.util.List;

/**
 * UserData
 *
 * @author f0rb on 2021-02-25
 */
public class UserData {
    public static final int INIT_SIZE = 5;

    public static UserController getUserController() {
        UserController userController = new UserController(new UserService(), new UserDetailService(), null);
        userController.create(getUserRequests());
        return userController;
    }

    private static List<UserRequest> getUserRequests() {
        List<UserRequest> userRequests = new ArrayList<>(INIT_SIZE);
        for (int i = 1; i < INIT_SIZE; i++) {
            UserRequest userRequest = new UserRequest();
            userRequest.setUsername("username" + i);
            userRequest.setPassword("password" + i);
            userRequest.setEmail("test" + i + "@163.com");
            userRequest.setMobile("1777888888" + i);
            userRequest.setValid(i % 2 == 0);
            userRequests.add(userRequest);
        }
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("f0rb");
        userRequest.setNickname("自在");
        userRequest.setPassword("123456");
        userRequest.setEmail("f0rb@163.com");
        userRequest.setMobile("17778888880");
        userRequests.add(userRequest);
        return userRequests;
    }
}
