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

package win.doyto.query.annotation;

import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Subquery
 *
 * @author f0rb on 2022/12/29
 * @since 1.0.1
 */
@SuppressWarnings("java:S1452")
@Target(FIELD)
@Retention(RUNTIME)
public @interface Subquery {

    String select();

    boolean distinct() default false;

    Class<? extends Persistable<? extends Serializable>>[] host() default {};

    Class<?>[] from();

}
