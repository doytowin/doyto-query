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

import com.mongodb.client.ClientSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.ResourceHolderSupport;

/**
 * MongoTransactionResource
 *
 * @author f0rb on 2022-06-27
 */
@Getter
@AllArgsConstructor
public class MongoSessionHolder extends ResourceHolderSupport {

    @NonNull
    private ClientSession session;

    public void setTimeoutIfNotDefaulted(int seconds) {
        if (seconds != TransactionDefinition.TIMEOUT_DEFAULT) {
            setTimeoutInSeconds(seconds);
        }
    }

}
