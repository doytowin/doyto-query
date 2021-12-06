package win.doyto.query.sql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * OrderByBuilder
 *
 * @author f0rb on 2020-01-01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderByBuilder {
    public static OrderByBuilder create() {
        return new OrderByBuilder();
    }

    private final StringBuilder buffer = new StringBuilder();

    public OrderByBuilder asc(String column) {
        buffer.append(column).append(",asc;");
        return this;
    }

    public OrderByBuilder desc(String column) {
        buffer.append(column).append(",desc;");
        return this;
    }

    public OrderByBuilder field(String column, String... args) {
        buffer.append("field(").append(column).append(',')
              .append(StringUtils.join(args, ',')).append(");");
        return this;
    }

    @Override
    public String toString() {
        return buffer.deleteCharAt(buffer.length() - 1).toString();
    }

}
