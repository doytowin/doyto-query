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

package win.doyto.query.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * To build a CASE clause to replace token {@code @Case}
 * in {@link Column#name()}.
 *
 * @author f0rb on 2024/11/20
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface Case {
    /**
     * WHEN ... THEN ... item
     */
    Item[] value();

    /**
     * @return an expression
     */
    String end() default "0";

    @interface Item {
        /**
         * A field of the query object to be resolved as a condition.
         *
         * @return a field name
         */
        String when();

        /**
         * @return an expression
         */
        String then();
    }
}
