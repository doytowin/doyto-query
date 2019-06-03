package win.doyto.query.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
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

    public SqlAndArgs(String sql, List<?> args) {
        this.sql = sql;
        this.args = args.toArray();
        if (log.isDebugEnabled() && !args.isEmpty()) {
            String out = args.stream()
                .map(arg -> arg + (arg == null ? "" : wrapWithParenthesis(arg.getClass().getName())) + SEPARATOR)
                .collect(Collectors.joining());
            log.debug("SQL  : {}", sql);
            log.debug("Param: {}", out.substring(0, out.lastIndexOf(',')));
        }
    }
}
