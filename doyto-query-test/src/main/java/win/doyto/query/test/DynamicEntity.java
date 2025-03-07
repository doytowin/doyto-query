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

package win.doyto.query.test;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.Column;
import win.doyto.query.annotation.Entity;
import win.doyto.query.annotation.Transient;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.entity.AbstractPersistable;

import java.util.Date;

/**
 * DynamicEntity
 *
 * @author f0rb on 2019-05-25
 */
@Getter
@Setter
@Entity(name = DynamicEntity.TABLE)
public class DynamicEntity extends AbstractPersistable<Integer> {
    public static final String TABLE = "t_dynamic_${user}_${project}";

    @Transient
    private String user;

    @Transient
    private String project;

    @Transient
    private String locale;

    @Column(name = "locale_${locale}")
    private String value;

    @Column(name = "user_score")
    private Integer score;

    private String memo;

    @Transient
    private Date createTime;

    @Override
    public IdWrapper<Integer> toIdWrapper() {
        return new DynamicIdWrapper(id, user, project, locale);
    }
}
