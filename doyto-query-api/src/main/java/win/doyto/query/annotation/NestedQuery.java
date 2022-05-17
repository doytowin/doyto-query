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

package win.doyto.query.annotation;

import java.lang.annotation.Target;

/**
 * SubQuery
 *
 * @author f0rb on 2019-05-28
 * @since 0.1.3
 * @deprecated from 0.3.1, use {@link win.doyto.query.annotation.DomainPath} for nested query
 */
@SuppressWarnings("java:S1133")
@Deprecated
@Target({})
public @interface NestedQuery {

    String select();

    String from();

    /**
     * @return some join clause.
     */
    String extra() default "";

    /**
     * Will use next @NestedQuery.select() as column if empty.
     *
     * @return custom column for next nested query.
     */
    String where() default "";

    String op() default "IN";

}
