package win.doyto.query.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * OrderBy
 *
 * @author f0rb on 2020-01-01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderBy {
    public static OrderBy create() {
        return new OrderBy();
    }

    private StringBuilder buffer = new StringBuilder();

    public OrderBy asc(String column) {
        buffer.append(column).append(",ASC;");
        return this;
    }

    public OrderBy desc(String column) {
        buffer.append(column).append(",DESC;");
        return this;
    }

    public OrderBy field(String input) {
        buffer.append("FIELD(").append(input).append(");");
        return this;
    }

    @Override
    public String toString() {
        return buffer.deleteCharAt(buffer.length() - 1).toString();
    }

}
