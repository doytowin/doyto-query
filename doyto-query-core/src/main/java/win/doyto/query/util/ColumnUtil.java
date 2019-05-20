package win.doyto.query.util;

import org.apache.commons.lang3.StringUtils;

/**
 * ColumnUtil
 *
 * @author f0rb
 */
public class ColumnUtil {
    private ColumnUtil() {
    }

    public static String escapeLike(String like) {
        if (StringUtils.isBlank(like)) {
            return like;
        }
        return "%" + like.replaceAll("[%|_]", "\\\\$0") + "%";
    }

}
