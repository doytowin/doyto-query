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

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * View
 *
 * @author f0rb on 2023/6/11
 * @since 1.0.2
 */
@Target(TYPE)
@Retention(RUNTIME)
@Repeatable(ComplexView.class)
public @interface View {

    Class<?> value();

    String alias() default "";

    /**
     * Map the current view depending on the type.
     * {@link ViewType#TABLE_NAME} as table name.
     * {@link ViewType#WITH} as `with` clause.
     *
     * @since 1.1.0/2.1.0
     */
    ViewType type() default ViewType.TABLE_NAME;

    /**
     * Set context to true to prevent the table name
     * from appearing in the FROM clause in a subquery.
     *
     * @since 1.1.0/2.1.0
     */
    boolean context() default false;

}
