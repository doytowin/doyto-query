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

package win.doyto.query.service;

import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.BeanFactory;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import javax.persistence.EntityType;

/**
 * DataAccessManager
 *
 * @author f0rb on 2022/11/26
 * @since 1.0.0
 */
@UtilityClass
class DataAccessManager {
    private final Map<EntityType, DataAccessFactory> dataAccessFactoryMap;

    static {
        dataAccessFactoryMap = new HashMap<>(4);
        ServiceLoader<DataAccessFactory> dataAccessProviders = ServiceLoader.load(DataAccessFactory.class);
        for (DataAccessFactory dataAccessFactory : dataAccessProviders) {
            dataAccessFactoryMap.put(dataAccessFactory.getEntityType(), dataAccessFactory);
        }
    }

    <E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
    DataAccess<E, I, Q> create(EntityType entityType, BeanFactory beanFactory, Class<E> entityClass) {
        return dataAccessFactoryMap.get(entityType).createDataAccess(beanFactory, entityClass);
    }
}