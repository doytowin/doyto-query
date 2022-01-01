/*
 * Copyright © 2019-2022 Forb Yuan
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

package win.doyto.query.test;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import win.doyto.query.core.IdWrapper;

import javax.persistence.Transient;

/**
 * DynamicIdWrapper
 *
 * @author f0rb
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DynamicIdWrapper implements IdWrapper<Integer> {

    private Integer id;

    @Transient
    private String user;

    @Transient
    private String project;

    private String locale;

}
