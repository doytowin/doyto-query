package win.doyto.query.web.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import win.doyto.query.web.response.ErrorCode;

import java.io.PrintWriter;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static win.doyto.query.web.WebMvcConfigurerAdapter.configObjectMapper;


/**
 * Created by IntelliJ IDEA.
 * Date: 2010-2-25
 * Time: 21:48:34
 *
 * @author f0rb
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpUtil {
    public static final String UNKNOWN = "unknown";
    private static final ObjectMapper objectMapper = configObjectMapper(new ObjectMapper());

    /**
     * 获取header信息，名字大小写无关.
     *
     * @param request HttpServletRequest Object
     * @param name    the name of the header
     * @return the header's value correspond to the name
     */
    public static String getHeader(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        if (value != null) {
            return value;
        }
        Enumeration<?> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String n = (String) names.nextElement();
            if (n.equalsIgnoreCase(name)) {
                return request.getHeader(n);
            }
        }
        return null;
    }

    @SneakyThrows
    public static void writeJson(HttpServletResponse response, Object content) {
        writeJson(response, objectMapper.writeValueAsString(content));
    }

    public static void writeJson(HttpServletResponse response, Enum<?> content) {
        writeJson(response, content instanceof ErrorCode ? ErrorCode.build((ErrorCode) content) : content);
    }

    @SneakyThrows
    public static void writeJson(HttpServletResponse response, String content) {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.print(content);
            out.flush();
        }
    }
}
