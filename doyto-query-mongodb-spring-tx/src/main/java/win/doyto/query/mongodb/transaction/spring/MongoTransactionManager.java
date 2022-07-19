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

import com.mongodb.MongoException;
import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.session.ServerSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ClassUtils;

/**
 * MongoTransactionManager
 *
 * @author f0rb on 2022-06-24
 */
@SuppressWarnings("java:S1948")
@Slf4j
public abstract class MongoTransactionManager extends AbstractPlatformTransactionManager {

    private final MongoClient mongoClient;

    private final TransactionOptions options;

    protected MongoTransactionManager(MongoClient mongoClient, TransactionOptions options) {
        this.mongoClient = mongoClient;
        this.options = options;
    }

    protected abstract ClientSession getClientSession();

    @Override
    protected Object doGetTransaction() throws TransactionException {
        MongoSessionHolder transaction = (MongoSessionHolder) TransactionSynchronizationManager.getResource(getResourceKey());
        return new MongoTransactionObject(transaction);
    }

    private Object getResourceKey() {
        return mongoClient;
    }

    @Override
    protected boolean isExistingTransaction(Object transaction) throws TransactionException {
        return castTransaction(transaction).isExistingTransaction();
    }

    private MongoTransactionObject castTransaction(Object transaction) {
        return (MongoTransactionObject) transaction;
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        MongoTransactionObject mongoTransactionObject = castTransaction(transaction);

        ClientSession clientSession = getClientSession();
        MongoSessionHolder mongoSessionHolder = new MongoSessionHolder(clientSession);
        mongoSessionHolder.setTimeoutIfNotDefaulted(determineTimeout(definition));

        mongoTransactionObject.setMongoSessionHolder(mongoSessionHolder);

        try {
            debug("About to start transaction for session %s.", clientSession);
            mongoTransactionObject.startTransaction(options);
            debug("Started transaction for session %s.", clientSession);
        } catch (MongoException e) {
            String msg = buildMessage("Could not start Mongo transaction for session %s.", clientSession);
            throw new TransactionSystemException(msg, e);
        }

        mongoSessionHolder.setSynchronizedWithTransaction(true);
        TransactionSynchronizationManager.bindResource(getResourceKey(), mongoSessionHolder);
    }

    @Override
    protected Object doSuspend(Object transaction) throws TransactionException {
        MongoTransactionObject mongoTransactionObject = castTransaction(transaction);
        mongoTransactionObject.setMongoSessionHolder(null);
        return TransactionSynchronizationManager.unbindResource(getResourceKey());
    }

    @Override
    protected void doResume(@Nullable Object transaction, Object suspendedResources) {
        TransactionSynchronizationManager.bindResource(getResourceKey(), suspendedResources);
    }

    @Override
	protected final void doCommit(DefaultTransactionStatus status) throws TransactionException {
        MongoTransactionObject mto = castTransaction(status.getTransaction());
        debug("About to commit transaction for session %s.", mto.getSession());

        try {
            mto.commit();
        } catch (Exception e) {
            String msg = buildMessage("Could not commit Mongo transaction for session %s.", mto.getSession());
            throw new TransactionSystemException(msg, e);
		}
	}

    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        MongoTransactionObject mto = castTransaction(status.getTransaction());
        debug("About to abort transaction for session %s.", mto.getSession());

        try {
            mto.rollback();
		} catch (MongoException e) {
            String message = buildMessage("Could not abort Mongo transaction for session %s.", mto.getSession());
            throw new TransactionSystemException(message, e);
		}
	}

    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
        MongoTransactionObject mto = castTransaction(status.getTransaction());
        mto.getMongoSessionHolder().setRollbackOnly();
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        MongoTransactionObject mto = castTransaction(transaction);

        // Remove the connection holder from the thread.
        TransactionSynchronizationManager.unbindResource(getResourceKey());
        mto.getMongoSessionHolder().clear();

        debug("About to release Session %s after transaction.", mto.getSession());
        mto.closeSession();
    }

    private void debug(String format, ClientSession clientSession) {
        if (log.isDebugEnabled()) {
            log.debug(buildMessage(format, clientSession));
        }
    }

    private String buildMessage(String format, ClientSession clientSession) {
        return String.format(format, toString(clientSession));
    }

    private static String toString(ClientSession session) {

        String debugString = String.format("[%s@%s ", ClassUtils.getShortName(session.getClass()),
                Integer.toHexString(session.hashCode()));

        try {
            ServerSession serverSession = session.getServerSession();
            if (serverSession != null) {
                debugString += String.format("id = %s, ", serverSession.getIdentifier());
                debugString += String.format("txNumber = %d, ", serverSession.getTransactionNumber());
                debugString += String.format("closed = %b, ", serverSession.isClosed());
            } else {
                debugString += "id = n/a";
            }
            debugString += String.format("causallyConsistent = %s, ", session.isCausallyConsistent());
            debugString += String.format("txActive = %s, ", session.hasActiveTransaction());
            debugString += String.format("clusterTime = %s", session.getClusterTime());
        } catch (RuntimeException e) {
            debugString += String.format("error = %s", e.getMessage());
        }

        debugString += "]";

        return debugString;
    }

}
