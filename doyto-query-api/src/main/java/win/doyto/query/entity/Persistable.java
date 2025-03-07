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

package win.doyto.query.entity;

import win.doyto.query.core.IdWrapper;

import java.beans.Transient;
import java.io.Serializable;

/**
 * Persistable
 *
 * @author f0rb
 */
public interface Persistable<I extends Serializable> extends Serializable {
    I getId();

    void setId(I id);

    @Transient
    default boolean isNew() {
        return getId() == null;
    }

    default IdWrapper<I> toIdWrapper() {
        return IdWrapper.build(getId());
    }
}
