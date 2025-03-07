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

package win.doyto.query.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.springframework.beans.factory.BeanFactory;
import win.doyto.query.annotation.EntityType;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.entity.Persistable;
import win.doyto.query.jdbc.rowmapper.RowMapper;
import win.doyto.query.service.DataAccessFactory;

import java.io.Serializable;

/**
 * JdbcDataAccessFactory
 *
 * @author f0rb on 2022/11/26
 * @since 1.0.0
 */
@Slf4j
public class JdbcDataAccessFactory implements DataAccessFactory {

    @Override
    public EntityType getEntityType() {
        return EntityType.RELATIONAL;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
    DataAccess<E, I, Q> createDataAccess(BeanFactory beanFactory, Class<E> entityClass) {
        DatabaseOperations databaseOperations = beanFactory.getBean(DatabaseOperations.class);
        String rowMapperName = entityClass.getCanonicalName().replace("Entity", "RowMapper");
        try {
            Class<?> clazz = Class.forName(rowMapperName);
            RowMapper<E> rowMapper = (RowMapper<E>) ConstructorUtils.invokeConstructor(clazz);
            return new JdbcDataAccess<>(databaseOperations, entityClass, rowMapper);
        } catch (Exception e) {
            log.debug("Initial RowMapper failed for {}, BeanPropertyRowMapper will be used: {}", rowMapperName, e.getMessage());
        }
        return new JdbcDataAccess<>(databaseOperations, entityClass);
    }

}
