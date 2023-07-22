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

package win.doyto.query.util.menu;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import win.doyto.query.core.IdWrapper;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuIdWrapper implements IdWrapper<Integer> {
    private Integer id;

    @JsonSerialize
    private String platform;

    @SuppressWarnings("unused")
    private String getPlatform() {
        return "01".equals(platform) ? "_" + platform : "";
    }

    @Override
    public String toCacheKey() {
        return id + "-" + platform;
    }
}
