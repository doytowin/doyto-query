package win.doyto.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.Resource;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * DemoApplicationTest
 *
 * @author f0rb
 * @date 2019-05-12
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes = DemoApplication.class)
class DemoApplicationTest {

    @Resource
    protected WebApplicationContext wac;
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void queryByUsername() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/query?username=f0rb"))
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$[0].nickname").value("测试1"))
        ;
    }

    @Test
    public void queryByAccount() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/query?account=17778888882"))
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$[0].nickname").value("测试2"))
               .andExpect(jsonPath("$[1]").doesNotExist())
        ;
    }

    @Test
    public void pageByAccount() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/page?account=17778888882&pageNumber=0&pageSize=5"))
               .andExpect(jsonPath("$.list").isArray())
               .andExpect(jsonPath("$.list[0].nickname").value("测试2"))
               .andExpect(jsonPath("$.total").value(1))
        ;
    }

    @Test
    public void pageMenu() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/menu/page?pageNumber=1&pageSize=2"))
               .andExpect(jsonPath("$.list").isArray())
               .andExpect(jsonPath("$.list[0]").doesNotExist())
               .andExpect(jsonPath("$.total").value(1))
        ;
    }
}