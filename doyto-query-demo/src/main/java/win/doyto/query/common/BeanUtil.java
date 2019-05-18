package win.doyto.query.common;

import com.alibaba.fastjson.JSON;

/**
 * BeanUtil
 *
 * @author f0rb
 * @date 2019-05-18
 */
public class BeanUtil {
    private BeanUtil() {
    }

    public static <T> T copyFields(Object source, Class<T> target) {
       return JSON.parseObject(JSON.toJSONString(source), target);
    }
}
