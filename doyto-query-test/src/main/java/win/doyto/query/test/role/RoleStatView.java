/*
 * Copyright © 2019-2025 DoytoWin, Inc.
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

package win.doyto.query.test.role;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.Entity;

import java.io.Serializable;

/**
 * RoleStatView
 *
 * @author f0rb on 2022/8/25
 * @since 1.0.0
 */
@Getter
@Setter
@Entity(name = "role")
public class RoleStatView implements Serializable {
    private Integer count;
}
