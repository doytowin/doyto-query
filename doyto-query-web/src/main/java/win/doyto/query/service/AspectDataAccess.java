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

package win.doyto.query.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.transaction.support.TransactionOperations;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.entity.EntityAspect;
import win.doyto.query.entity.Persistable;

import java.io.Serializable;
import java.util.List;

/**
 * AspectDataAccess
 *
 * @author f0rb on 2023/6/16
 * @since 1.0.2
 */
@SuppressWarnings({"DataFlowIssue", "java:S2259"})
@AllArgsConstructor
public class AspectDataAccess<E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
        implements DataAccess<E, I, Q> {

    @Delegate(excludes = ExcludedDataAccess.class)
    private final DataAccess<E, I, Q> delegate;
    private final List<EntityAspect<E>> entityAspects;
    private final TransactionOperations transactionOperations;

    @Override
    public void create(E e) {
        transactionOperations.execute(s -> {
            delegate.create(e);
            entityAspects.forEach(entityAspect -> entityAspect.afterCreate(e));
            return null;
        });
    }

    @Override
    public int update(E e) {
        return transactionOperations.execute(s -> {
            E origin = delegate.get(e.toIdWrapper());
            if (origin == null) {
                return 0;
            }
            delegate.update(e);
            E current = delegate.get(e.toIdWrapper());
            entityAspects.forEach(entityAspect -> entityAspect.afterUpdate(origin, current));
            return 1;
        });
    }

    @Override
    public int patch(E e) {
        return transactionOperations.execute(s -> {
            E origin = delegate.get(e.toIdWrapper());
            if (origin == null) {
                return 0;
            }
            delegate.patch(e);
            E current = delegate.get(e.toIdWrapper());
            entityAspects.forEach(entityAspect -> entityAspect.afterUpdate(origin, current));
            return 1;
        });
    }

    @Override
    public int delete(IdWrapper<I> w) {
        return transactionOperations.execute(s -> {
            E e = delegate.get(w);
            if (e == null) {
                return 0;
            }
            int deleted = delegate.delete(w);
            entityAspects.forEach(entityAspect -> entityAspect.afterDelete(e));
            return deleted;
        });
    }


    @SuppressWarnings({"unused", "java:S1610"})
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private abstract class ExcludedDataAccess {
        public abstract void create(E e);

        public abstract void update(E e);

        public abstract void patch(E e);

        public abstract int delete(IdWrapper<I> w);
    }

}
