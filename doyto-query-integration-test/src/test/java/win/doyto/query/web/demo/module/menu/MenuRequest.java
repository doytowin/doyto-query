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

package win.doyto.query.web.demo.module.menu;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.validation.PatchGroup;
import win.doyto.query.validation.UpdateGroup;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * MenuRequest
 *
 * @author f0rb
 */
@Getter
@Setter
public class MenuRequest {

    @NotNull(groups = {UpdateGroup.class, PatchGroup.class})
    private Integer id;

    @NotBlank
    private String platform;

    private Integer parentId;

    private String menuName;

    private String memo;

    private Boolean valid = true;

}
