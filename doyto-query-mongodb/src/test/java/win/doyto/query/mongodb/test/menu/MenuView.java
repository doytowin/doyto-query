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

package win.doyto.query.mongodb.test.menu;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.mongodb.entity.MongoPersistable;

import java.math.BigInteger;

/**
 * MenuView
 *
 * @author f0rb on 2022-05-21
 * @since 1.0.0
 */
@Getter
@Setter
public class MenuView extends MongoPersistable<BigInteger> {
    private String menuName;
    private String menuCode;
    private Boolean valid;
}