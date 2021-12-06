package win.doyto.query.sql;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * SqlAndArgs
 *
 * @author f0rb on 2019-05-31
 */
@Slf4j
@Getter
public class SqlAndArgs {
    private final String sql;
    private final Object[] args;

    public SqlAndArgs(String sql, List<?> argList) {
        this(sql, argList.toArray());
    }

    public SqlAndArgs(String sql, Object... args) {
        this.sql = sql;
        this.args = args;
        logSqlInfo();
    }

    private void logSqlInfo() {
        if (log.isDebugEnabled()) {
            log.debug("SQL  : {}", sql);
            String params = Arrays
                    .stream(args)
                    .map(arg -> arg + (arg == null ? "" : "(" + arg.getClass().getName() + ")"))
                    .collect(Collectors.joining(", "));
            log.debug("Param: {}", params);
        }
    }

    static SqlAndArgs buildSqlWithArgs(Function<List<Object>, String> sqlBuilder) {
        List<Object> argList = new ArrayList<>();
        String sql = sqlBuilder.apply(argList);
        return new SqlAndArgs(sql, argList);
    }
}
