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

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import de.flapdoodle.embed.mongo.config.*;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import win.doyto.query.mongodb.session.MongoSessionSupplier;
import win.doyto.query.mongodb.session.MongoSessionThreadLocalSupplier;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * MongoTransactionApplication
 *
 * @author f0rb on 2022-06-23
 */
@EnableRetry
@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true)
@AutoConfigureAfter(value = EmbeddedMongoAutoConfiguration.class)
public class MongoTransactionApplication {
    public static void main(String[] args) {
        SpringApplication.run(MongoTransactionApplication.class);
    }


    @Bean
    public MongoSessionSupplier mongoSessionSupplier(MongoClient mongoClient) {
        return MongoSessionThreadLocalSupplier.create(mongoClient);
    }

    @Bean
    public PlatformTransactionManager mongoTransactionManager(
            MongoClient mongoClient, MongoSessionSupplier mongoSessionSupplier) {
        TransactionOptions txOptions = TransactionOptions
                .builder()
                .readPreference(ReadPreference.primary())
                .readConcern(ReadConcern.LOCAL)
                .writeConcern(WriteConcern.MAJORITY)
                .build();
        return new MongoTransactionManager(mongoClient, txOptions) {
            @Override
            protected ClientSession getClientSession() {
                return mongoSessionSupplier.get();
            }

            @Override
            protected void doCleanupAfterCompletion(Object transaction) {
                super.doCleanupAfterCompletion(transaction);
                mongoSessionSupplier.close();
            }
        };
    }

    @Slf4j
    @AllArgsConstructor
    @Configuration
    @AutoConfigureBefore(EmbeddedMongoAutoConfiguration.class)
    @EnableConfigurationProperties({MongoProperties.class, EmbeddedMongoProperties.class})
    static class EmbeddedMongoConfig {

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

}