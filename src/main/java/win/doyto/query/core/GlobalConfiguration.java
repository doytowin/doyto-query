package win.doyto.query.core;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

/**
 * GlobalConfiguration
 *
 * @author f0rb
 */
@Getter
@Setter
public class GlobalConfiguration {

    private static final Pattern PTN_CAPITAL_CHAR = Pattern.compile("([A-Z])");

    private boolean mapCamelCaseToUnderscore;

    private GlobalConfiguration() {
    }

    static String convertColumn(String columnName) {
        if (instance().isMapCamelCaseToUnderscore()) {
            columnName = camelCaseToUnderscore(columnName);
        }
        return columnName;
    }

    private static String camelCaseToUnderscore(String camel) {
        return PTN_CAPITAL_CHAR.matcher(camel).replaceAll("_$1").toLowerCase();
    }

    public static GlobalConfiguration instance() {
        return GlobalConfiguration.Singleton.instance;
    }

    private static class Singleton {
        private static GlobalConfiguration instance = new GlobalConfiguration();
    }

}
