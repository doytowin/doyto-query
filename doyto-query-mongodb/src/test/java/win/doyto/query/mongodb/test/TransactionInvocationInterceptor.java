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

package win.doyto.query.mongodb.test;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import win.doyto.query.mongodb.session.MongoSessionSupplier;
import win.doyto.query.mongodb.session.MongoSessionThreadLocalSupplier;
import win.doyto.query.util.CommonUtil;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TransactionInvocation
 *
 * @author f0rb on 2022/7/16
 * @since 1.0.0
 */
public class TransactionInvocationInterceptor implements InvocationInterceptor, BeforeAllCallback {

    MongoSessionSupplier mongoSessionSupplier;

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        ApplicationContext ctx = SpringExtension.getApplicationContext(extensionContext);
        MongoClient mongoClient = ctx.getBean(MongoClient.class);
        this.mongoSessionSupplier = MongoSessionThreadLocalSupplier.create(mongoClient);
    }

    @Override
    public void interceptTestMethod(
            Invocation<Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext
    ) {
        ClientSession clientSession = mongoSessionSupplier.get();
        RuntimeException abort = new RuntimeException("Abort");
        try {
            clientSession.withTransaction(() -> {
                try {
                    invocation.proceed();
                } catch (Throwable t) {
                    try {
                        AtomicBoolean invokedOrSkipped = (AtomicBoolean) CommonUtil.readField(invocation, "invokedOrSkipped");
                        invokedOrSkipped.set(false);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    throw t instanceof RuntimeException ? (RuntimeException) t : new RuntimeException(t);
                }
                throw abort;
            });
        } catch (Exception e) {
            if (abort != e) {
                throw e;
            }
        }
    }
}