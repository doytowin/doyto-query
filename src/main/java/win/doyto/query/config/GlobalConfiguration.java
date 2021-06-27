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

    private boolean startPageNumberFromOne;

    private Dialect dialect = new MySQLDialect();

    public static int calcStartPageNumber(Integer page) {
        return instance().isStartPageNumberFromOne() ? Math.max(page - 1, 0) : page;
    }

    public static GlobalConfiguration instance() {
        return Singleton.instance;
    }

    private static class Singleton {
        private static final GlobalConfiguration instance = new GlobalConfiguration();
    }

    public static Dialect dialect() {
        return instance().dialect;
    }

    public void setStartPageNumberFromOne(boolean startPageNumberFromOne) {
        this.startPageNumberFromOne = startPageNumberFromOne;
    }

}
