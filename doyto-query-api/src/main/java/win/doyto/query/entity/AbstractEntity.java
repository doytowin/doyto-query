/*
 * Copyright © 2019-2024 Forb Yuan
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

import java.io.Serial;
import java.io.Serializable;

/**
 * AbstractCommonEntity
 *
 * @param <I> the type of entity id
 * @param <U> the type of user id
 * @author f0rb on 2022-03-27
 */
@Getter
@Setter
public abstract class AbstractEntity<I extends Serializable, U extends Serializable, D extends Serializable>
        extends AbstractPersistable<I> implements Serializable, CreateUserAware<U>, UpdateUserAware<U> {

    @Serial
    private static final long serialVersionUID = 1;

    /**
     * 创建者
     */
    private U createUserId;
    /**
     * 创建时间
     */
    private D createTime;
    /**
     * 更新者
     */
    private U updateUserId;
    /**
     * 更新时间
     */
    private D updateTime;

    public void setCreateUserId(U createUserId) {
        this.createUserId = createUserId;
        this.setCreateTime(current());
    }

    public void setUpdateUserId(U updateUserId) {
        this.updateUserId = updateUserId;
        this.setUpdateTime(current());
    }

    protected abstract D current();

}
