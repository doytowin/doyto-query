package win.doyto.query.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static win.doyto.query.core.CommonUtil.wrapWithParenthesis;
import static win.doyto.query.core.Constant.SEPARATOR;

/**
 * SqlAndArgs
 *
 * @author f0rb on 2019-05-31
 */
@Slf4j
@Getter
public class SqlAndArgs {
    String sql;
    Object[] args;

    public SqlAndArgs(String sql, List<?> argList) {
        this.sql = sql;
        this.args = argList.toArray();
        logSqlInfo(sql, argList);
    }

    public static void logSqlInfo(String sql, List<?> argList) {
        if (log.isDebugEnabled()) {
            log.debug("SQL  : {}", sql);
            String params = argList
                    .stream()
                    .map(arg -> arg + (arg == null ? "" : wrapWithParenthesis(arg.getClass().getName())))
                    .collect(Collectors.joining(SEPARATOR));
            log.debug("Param: {}", params);
        }
    }

    static SqlAndArgs buildSqlWithArgs(Function<List<Object>, String> sqlBuilder) {
        List<Object> argList = new ArrayList<>();
        String sql = sqlBuilder.apply(argList);
        return new SqlAndArgs(sql, argList);
    }
}
