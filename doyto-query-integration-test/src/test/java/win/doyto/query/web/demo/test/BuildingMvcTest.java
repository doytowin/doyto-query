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

package win.doyto.query.web.demo.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * BuildingMvcTest
 *
 * @author f0rb on 2021-12-08
 */
class BuildingMvcTest extends DemoApplicationTest {

    @BeforeEach
    void setUp() throws Exception {
        String data = "[{\"name\": \"Times Building\", \"loc\": [1, 2]}, {\"name\": \"Times Station\", \"loc\": [3, 2]}]";
        performAndExpectSuccess(buildJson(post("/building/"), data));
    }

    @Test
    void getBuilding() throws Exception {
        performAndExpectSuccess(get("/building/"))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list[0].id").exists())
                .andExpect(jsonPath("$.data.list[0].name").value("Times Building"))
        ;

        String json = "{\"coordinates\":[[1,2],[2,2]],\"type\":\"line\"}";
        performAndExpectSuccess(get("/building/").param("locIntX",json))
                .andExpect(jsonPath("$.data.total").value(1))
        ;
    }
}
