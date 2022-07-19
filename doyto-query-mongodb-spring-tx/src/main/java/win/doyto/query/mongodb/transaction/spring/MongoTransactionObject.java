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

package win.doyto.query.mongodb.transaction.spring;

import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;
import com.mongodb.session.ServerSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.transaction.support.SmartTransactionObject;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

/**
 * MongoTransactionObject
 *
 * @author f0rb on 2022-07-01
 */
@Getter
@Setter
@AllArgsConstructor
public class MongoTransactionObject implements SmartTransactionObject {
    private MongoSessionHolder mongoSessionHolder;

    public boolean isExistingTransaction() {
        return mongoSessionHolder != null;
    }

    public ClientSession getSession() {
        return mongoSessionHolder.getSession();
    }

    void startTransaction(TransactionOptions options) {
        getSession().startTransaction(options);
    }

    void commit() {
        getSession().commitTransaction();
    }

    void rollback() {
        getSession().abortTransaction();
    }

    void closeSession() {
        ClientSession clientSession = getSession();
        ServerSession serverSession = clientSession.getServerSession();
        if (serverSession != null && !serverSession.isClosed()) {
            clientSession.close();
        }
    }

    @Override
    public boolean isRollbackOnly() {
        return this.mongoSessionHolder != null && this.mongoSessionHolder.isRollbackOnly();
    }

    @Override
    public void flush() {
        TransactionSynchronizationUtils.triggerFlush();
    }
}
