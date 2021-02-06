package win.doyto.query.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import win.doyto.query.core.Dialect;

/**
 * GlobalConfiguration
 *
 * @author f0rb
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GlobalConfiguration {

    private boolean mapCamelCaseToUnderscore;
    private boolean ignoreCacheException = true;
    private Dialect dialect = new MySQLDialect();

    public static GlobalConfiguration instance() {
        return Singleton.instance;
    }

    private static class Singleton {
        private static final GlobalConfiguration instance = new GlobalConfiguration();
    }

    public static Dialect dialect() {
        return instance().dialect;
    }
}
