/*
 * Copyright © 2019-2022 Forb Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.doyto.query.web.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import win.doyto.query.util.BeanUtil;
import win.doyto.query.web.response.ErrorCode;

import java.io.PrintWriter;
import java.util.Enumeration;

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
        writeJson(response, BeanUtil.stringify(content));
    }

    public static void writeJson(HttpServletResponse response, Enum<?> content) {
        writeJson(response, content instanceof ErrorCode errorCode ? ErrorCode.build(errorCode) : content);
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
