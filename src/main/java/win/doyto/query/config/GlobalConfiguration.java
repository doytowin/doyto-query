package win.doyto.query.config;

import lombok.Getter;
import lombok.Setter;

/**
 * GlobalConfiguration
 *
 * @author f0rb
 */
@Getter
@Setter
public class GlobalConfiguration {

    private boolean mapCamelCaseToUnderscore;

    private GlobalConfiguration() {
    }

    public static GlobalConfiguration instance() {
        return GlobalConfiguration.Singleton.instance;
    }

    private static class Singleton {
        private static GlobalConfiguration instance = new GlobalConfiguration();
    }

}
