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

package win.doyto.query.mongodb;

import lombok.experimental.UtilityClass;

/**
 * MongoConstant
 *
 * @author f0rb on 2022-06-14
 * @since 1.0.0
 */
@UtilityClass
public class MongoConstant {
    public static final String MONGO_ID = "_id";
    public static final String COUNT_KEY = "count";

    public static String ex(String field) {
        return "$" + field;
    }

}
