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

package win.doyto.query.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.annotation.GeneratedValue;
import win.doyto.query.annotation.Id;
import win.doyto.query.validation.CreateGroup;
import win.doyto.query.validation.PatchGroup;
import win.doyto.query.validation.UpdateGroup;

import java.io.Serial;
import java.io.Serializable;

/**
 * AbstractId
 *
 * @author f0rb on 2021-06-27
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractPersistable<I extends Serializable> implements Persistable<I>, Serializable {
    @Serial
    private static final long serialVersionUID = 1;
    @Id
    @GeneratedValue
    @Null(groups = CreateGroup.class)
    @NotNull(groups = {UpdateGroup.class, PatchGroup.class})
    protected I id;
}
