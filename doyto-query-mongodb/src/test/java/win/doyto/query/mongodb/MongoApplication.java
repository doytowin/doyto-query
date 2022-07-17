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

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * MongoApplication
 *
 * @author f0rb on 2022-03-17
 */
@SpringBootApplication
public class MongoApplication {
    public static void main(String[] args) {
        SpringApplication.run(MongoApplication.class);
    }

    @Slf4j
    @AllArgsConstructor
    @Configuration
    @AutoConfigureBefore(EmbeddedMongoAutoConfiguration.class)
    @EnableConfigurationProperties({MongoProperties.class, EmbeddedMongoProperties.class})
    static class MongoConfig {

        private static final byte[] IP4_LOOPBACK_ADDRESS = new byte[]{127, 0, 0, 1};
        private static final byte[] IP6_LOOPBACK_ADDRESS = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
        private MongoProperties properties;

        private static InetAddress getHost(String host) throws UnknownHostException {
            return host == null
                    ? InetAddress.getByAddress(Network.localhostIsIPv6() ? IP6_LOOPBACK_ADDRESS : IP4_LOOPBACK_ADDRESS)
                    : InetAddress.getByName(host);
        }

        @Bean
        public MongodConfig embeddedMongoConfiguration(EmbeddedMongoProperties embeddedProperties) throws IOException {
            ImmutableMongodConfig.Builder builder = MongodConfig.builder().version(Version.V5_0_5);
            EmbeddedMongoProperties.Storage storage = embeddedProperties.getStorage();
            String databaseDir = storage.getDatabaseDir();
            String replSetName = storage.getReplSetName();
            int oplogSize = (storage.getOplogSize() != null) ? (int) storage.getOplogSize().toMegabytes() : 0;
            builder.replication(new Storage(databaseDir, replSetName, oplogSize));

            // This line enables the required journaling. This line is missing from actual spring boot's implementation.
            builder.cmdOptions(MongoCmdOptions.builder().useNoJournal(false).build());

            InetAddress host = getHost(this.properties.getHost());
            Integer configuredPort = this.properties.getPort();
            if (configuredPort != null && configuredPort > 0) {
                builder.net(new Net(host.getHostAddress(), configuredPort, Network.localhostIsIPv6()));
            } else {
                builder.net(new Net(host.getHostAddress(), Network.freeServerPort(host), Network.localhostIsIPv6()));
            }

            return builder.build();
        }
    }

    @Slf4j
    @Configuration
    @AllArgsConstructor
    static class UTConfig implements InitializingBean {

        MongodExecutable embeddedMongoServer;
        MongodConfig mongodConfig;

        public void afterPropertiesSet() throws Exception {
            String host = mongodConfig.net().getBindIp() + ":" + mongodConfig.net().getPort();
            String uri = String.format("mongodb://%s/admin?ReadPreference=primary", host);
            try (MongoClient mongoClient = MongoClients.create(uri)) {
                MongoDatabase admin = mongoClient.getDatabase("admin");
                initReplica(host, admin);
                waitUntilReplicaReady(admin);
            }
        }

        private void initReplica(String host, MongoDatabase admin) {
            Document initDoc = new Document();
            initDoc.append("_id", mongodConfig.replication().getReplSetName());
            Document member0 = new Document();
            member0.append("_id", 0);
            member0.append("host", host);
            initDoc.append("members", Arrays.asList(member0));

            Document replSetInitiate = new Document("replSetInitiate", initDoc);
            admin.runCommand(replSetInitiate);
        }

        private void waitUntilReplicaReady(MongoDatabase admin) throws InterruptedException {
            Document document;
            do {
                document = getReplicaStatus(admin);
            } while (isReplicaInitializing(document));
        }

        private boolean isReplicaInitializing(Document document) {
            return document.get("lastStableRecoveryTimestamp", BsonTimestamp.class).getTime() == 0;
        }

        private Document getReplicaStatus(MongoDatabase admin) {
            Document replSetGetStatus = new Document("replSetGetStatus", "1");
            return admin.runCommand(replSetGetStatus);
        }
    }

}
