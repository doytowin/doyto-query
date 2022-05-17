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

package win.doyto.query.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * CommonEntity
 *
 * @param <I> the type of entity id
 * @param <U> the type of user id
 * @author f0rb
 * @deprecated due to {@link java.util.Date}. Switch to
 * {@link AbstractEntity} or {@link AbstractCommonEntity}
 */
@Getter
@Setter
@Deprecated
@SuppressWarnings("java:S1133")
public abstract class CommonEntity<I extends Serializable, U extends Serializable>
        extends AbstractEntity<I, U, Date> {

    private static final long serialVersionUID = 1;

    @Override
    protected Date current() {
        return new Date();
    }
}
