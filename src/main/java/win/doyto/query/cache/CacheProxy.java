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
            log.error("{}#{}[cache={}, key={}] failed: {}",
                      delegate.getClass().getName(), method.getName(), delegate.getName(), args.length > 0 ? args[0] : "",
                      e.getTargetException().getMessage()
            );
            return null;
        }
    }

}
