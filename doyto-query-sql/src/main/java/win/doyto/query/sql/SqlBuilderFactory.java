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

package win.doyto.query.sql;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import win.doyto.query.entity.Persistable;

/**
 * SqlBuilderFactory
 *
 * @author f0rb on 2021-11-21
 */
@SuppressWarnings("unchecked")
@Slf4j
@UtilityClass
public class SqlBuilderFactory {
    public static <E extends Persistable<?>> SqlBuilder<E> create(Class<E> entityClass) {
        String queryBuilderName = entityClass.getCanonicalName().replace("Entity", "QueryBuilder");
        try {
            Class<?> clazz = Class.forName(queryBuilderName);
            Object target = ConstructorUtils.invokeConstructor(clazz, entityClass);
            return (SqlBuilder<E>) target;
        } catch (Exception e) {
            log.debug("Construct failed for: {}, the default QueryBuilder would be used: {}", queryBuilderName, e.getMessage());
        }
        return new CrudBuilder<>(entityClass);
    }
}
