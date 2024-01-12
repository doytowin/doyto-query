/*
 * Copyright © 2019-2024 Forb Yuan
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

package win.doyto.query.cache;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * CacheProxy
 *
 * @author f0rb on 2018-08-01.
 */
@Slf4j
@AllArgsConstructor
public class CacheProxy implements InvocationHandler {

    @NonNull
    private Cache delegate;

    public static Cache wrap(Cache cache) {
        return (Cache) Proxy.newProxyInstance(CacheProxy.class.getClassLoader(), new Class[]{Cache.class}, new CacheProxy(cache));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(delegate, args);
        } catch (InvocationTargetException e) {
            log.error("{}#{}[cache={}, args={}] failed: {}",
                      delegate.getClass().getName(), method.getName(), delegate.getName(), args,
                      e.getTargetException().getMessage()
            );
            return null;
        }
    }

}
