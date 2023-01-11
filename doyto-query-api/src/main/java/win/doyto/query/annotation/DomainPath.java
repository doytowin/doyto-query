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

package win.doyto.query.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * DomainPath
 *
 * @author f0rb on 2022-04-08
 * @since 0.3.1
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface DomainPath {
    /**
     * To describe how to route from the host domain to the target domain.
     *
     * @return paths array
     */
    String[] value();

    /**
     * The field in this domain to maintain the relationship with the target domain.
     *
     * @return name of the local field
     */
    String localField() default "id";

    /**
     * The field in another domain to maintain the relationship with this domain.
     *
     * @return name of the foreign field
     */
    String foreignField() default "id";
}
