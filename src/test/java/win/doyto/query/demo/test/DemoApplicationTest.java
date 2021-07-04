package win.doyto.query.demo.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import win.doyto.query.demo.DemoApplication;

import javax.annotation.Resource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * DemoApplicationTest
 *
 * @author f0rb
 */
@Transactional
@Rollback
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes = DemoApplication.class)
public abstract class DemoApplicationTest {

    @Resource
    protected WebApplicationContext wac;
    protected MockMvc mockMvc;
    protected MockHttpSession session;

    protected ResultActions requestJson(MockHttpServletRequestBuilder builder, String content, MockHttpSession session) throws Exception {
        return mockMvc.perform(builder.content(content).contentType(MediaType.APPLICATION_JSON_UTF8).session(session));
    }

    protected ResultActions requestJson(MockHttpServletRequestBuilder builder, String content) throws Exception {
        return requestJson(builder, content, new MockHttpSession());
    }

    protected ResultMatcher statusIs200() {
        return status().isOk();
    }

    protected ResultActions performAndExpectOk(RequestBuilder requestBuilder) throws Exception {
        return mockMvc.perform(requestBuilder)
                      .andDo(print())
                      .andExpect(statusIs200());
    }

    protected MockHttpServletRequestBuilder buildJson(MockHttpServletRequestBuilder builder, String content) {
        return builder.content(content).contentType("application/json;charset=UTF-8").session(session);
    }

    @BeforeEach
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        MvcResult mvcResult = requestJson(post("/login"), "{\"account\":\"f0rb\",\"password\":\"123456\"}")
                .andExpect(statusIs200()).andReturn();
        session = (MockHttpSession) mvcResult.getRequest().getSession();
    }

}