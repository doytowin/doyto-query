package win.doyto.query.core;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import static win.doyto.query.core.CommonUtil.wrapWithParenthesis;

/**
 * SqlAndArgs
 *
 * @author f0rb on 2019-05-31
 */
@Slf4j
class SqlAndArgs {
    String sql;
    Object[] args;

    public SqlAndArgs(String sql, List<?> args) {
        this.sql = sql;
        this.args = args.toArray();
        if (log.isDebugEnabled() && !args.isEmpty()) {
            String out = args.stream()
                .map(arg -> arg + (arg == null ? "" : wrapWithParenthesis(arg.getClass().getName())) + ", ")
                .collect(Collectors.joining());
            log.debug("params: {}", out.substring(0, out.lastIndexOf(',')));
        }
    }
}
