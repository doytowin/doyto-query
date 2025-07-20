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

package win.doyto.query.core;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * IdWrapper
 *
 * @author f0rb on 2020-02-07
 * @since 0.3.0
 */
public interface IdWrapper<I extends Serializable> {
    I getId();

    default String toCacheKey() {
        return String.valueOf(getId());
    }

    static <T extends Serializable> IdWrapper.Simple<T> build(T id) {
        return new Simple<>(id);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    final class Simple<I extends Serializable> implements IdWrapper<I> {
        private I id;
    }
}
