package win.doyto.query.web.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import win.doyto.query.web.response.PresetErrorCode;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HttpUtilTest
 *
 * @author f0rb on 2020-04-19
 */
class HttpUtilTest {
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void getHeader() {
        request.addHeader(HttpHeaders.ACCEPT, "application/json;charset=UTF-8∂");
        assertTrue(HttpUtil.getHeader(request, HttpHeaders.ACCEPT).startsWith(MediaType.APPLICATION_JSON_VALUE));

        assertNull(HttpUtil.getHeader(request, HttpHeaders.ACCEPT_CHARSET));
    }

    @Test
    void writeJson() throws UnsupportedEncodingException {
        HttpUtil.writeJson(response, PresetErrorCode.ERROR);
        assertEquals("{\"code\":1,\"message\":\"系统内部异常\",\"success\":false}", response.getContentAsString());
    }
}