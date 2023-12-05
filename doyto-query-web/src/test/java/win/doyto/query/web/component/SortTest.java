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

package win.doyto.query.web.component;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * SortTest
 *
 * @author f0rb on 2023/11/30
 * @since 1.0.3
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class SortTest {

    @Resource
    protected MockMvc mockMvc;

    @Test
    void shouldReadSortFromSubSortParamIfProvided() throws Exception {
        mockMvc.perform(get("/sort?id=4&sort.id=asc&sort=id,desc"))
               .andExpect(jsonPath("$.success").value(true))
               .andExpect(jsonPath("$.data").value("id,asc"));
    }

    @Test
    void shouldBeOKWhenSubSortParamNotProvided() throws Exception {
        mockMvc.perform(get("/sort"))
               .andExpect(jsonPath("$.success").value(true))
               .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void shouldBeConnectedBySemicolonIfMultiSortParamProvided() throws Exception {
        mockMvc.perform(get("/sort?sort.id=desc&sort.username=asc"))
               .andExpect(jsonPath("$.success").value(true))
               .andExpect(jsonPath("$.data").value("id,desc;username,asc"));
    }

    @Test
    void shouldResolvedAsFieldIfSubSortParamContainComma() throws Exception {
        mockMvc.perform(get("/sort?sort.id=desc&sort.userLevel=NORMAL,VIP"))
               .andExpect(jsonPath("$.success").value(true))
               .andExpect(jsonPath("$.data").value("id,desc;field(userLevel,NORMAL,VIP)"));
    }

    @Test
    void shouldSupportEmptySubSortParam() throws Exception {
        mockMvc.perform(get("/sort?sort.id&sort.username"))
               .andDo(print())
               .andExpect(jsonPath("$.success").value(true))
               .andExpect(jsonPath("$.data").value("id,asc;username,asc"));
    }

    @Test
    void shouldSupportCustomColumnsWhenConfigured() throws Exception {
        mockMvc.perform(get("/sort?sort.username&sort.memo=desc"))
               .andDo(print())
               .andExpect(jsonPath("$.success").value(true))
               .andExpect(jsonPath("$.data").value("username,asc"));
    }

    @Test
    void shouldSupportAnyColumnsWhenNotConfigured() throws Exception {
        mockMvc.perform(get("/sort2?sort.username&sort.memo=desc"))
               .andDo(print())
               .andExpect(jsonPath("$.success").value(true))
               .andExpect(jsonPath("$.data").value("username,asc;memo,desc"));
    }

    @Test
    void shouldBeNullWhenNoSortableColumns() throws Exception {
        mockMvc.perform(get("/sort?sort.memo=desc&score"))
               .andDo(print())
               .andExpect(jsonPath("$.success").value(true))
               .andExpect(jsonPath("$.data").doesNotExist());
    }

}
