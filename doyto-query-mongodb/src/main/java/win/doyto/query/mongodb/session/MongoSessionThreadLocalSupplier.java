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

package win.doyto.query.mongodb.session;

import com.mongodb.ClientSessionOptions;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MongoSessionThreadLocalProvider
 *
 * @author f0rb on 2022-07-11
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MongoSessionThreadLocalSupplier implements MongoSessionSupplier {

    private static final ThreadLocal<Map<MongoClient, ClientSession>> CLIENT_SESSION_THREAD_LOCAL = new ThreadLocal<>();
    private static final int INITIAL_CAPACITY = 4;
    private static final Map<MongoClient, MongoSessionSupplier> MONGO_SESSION_SUPPLIER_MAP = new ConcurrentHashMap<>(INITIAL_CAPACITY);

    @Getter
    private MongoClient mongoClient;

    public static MongoSessionSupplier create(MongoClient mongoClient) {
        return MONGO_SESSION_SUPPLIER_MAP.computeIfAbsent(mongoClient, MongoSessionThreadLocalSupplier::new);
    }

    @Override
    public ClientSession get() {
        Map<MongoClient, ClientSession> map = CLIENT_SESSION_THREAD_LOCAL.get();
        if (map == null) {
            map = new HashMap<>(INITIAL_CAPACITY);
            CLIENT_SESSION_THREAD_LOCAL.set(map);
        }
        return map.computeIfAbsent(mongoClient, c -> c.startSession(
                ClientSessionOptions.builder().causallyConsistent(true).build())
        );
    }

    @Override
    public void close() {
        Map<MongoClient, ClientSession> map = CLIENT_SESSION_THREAD_LOCAL.get();
        for (ClientSession clientSession : map.values()) {
            clientSession.close();
        }
        map.clear();
        CLIENT_SESSION_THREAD_LOCAL.remove();
    }
}
