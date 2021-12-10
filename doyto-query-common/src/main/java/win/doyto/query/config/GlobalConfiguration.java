package win.doyto.query.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import win.doyto.query.core.Dialect;

import java.util.function.Function;

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
    private Dialect dialect = (sql, limit, offset) ->
            sql + " LIMIT " + limit + (sql.startsWith("SELECT") ? " OFFSET " + offset : "");
    private Function<Integer, Integer> startPageNumberAdjuster;

    public static int adjustStartPageNumber(Integer page) {
        return instance().getStartPageNumberAdjuster().apply(page);
    }

    public static GlobalConfiguration instance() {
        return Singleton.instance;
    }

    private static class Singleton {
        private static final GlobalConfiguration instance = new GlobalConfiguration();

        static {
            instance.setStartPageNumberFromOne(false);
        }
    }

    public static Dialect dialect() {
        return instance().dialect;
    }

    public void setStartPageNumberFromOne(boolean startPageNumberFromOne) {
        instance().setStartPageNumberAdjuster(page -> startPageNumberFromOne ? Math.max(page - 1, 0) : page);
    }

}
