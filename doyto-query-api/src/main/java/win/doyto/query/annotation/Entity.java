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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Entity
 *
 * @author f0rb on 2022-05-19
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Entity {

    EntityType type() default EntityType.RELATIONAL;

    /**
     * For relational database:
     * <p>
     * (Optional) The schema of the table.
     * Defaults to the default schema for user.
     * <p>
     * For MongoDb:
     * (Required) The database of the collection.
     */
    String database() default "";

    /**
     * Table name for relational database.
     * Collection name for MongoDB.
     */
    String name();
}
