package win.doyto.query.validation;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.util.NestedServletException;
import win.doyto.query.demo.test.DemoApplicationTest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ValidationGroupTest
 *
 * @author f0rb on 2021-07-02
 */
class ValidationGroupTest extends DemoApplicationTest {

    @Test
    void shouldRejectWhenCreateContentContainsId() {
        MockHttpServletRequestBuilder requestBuilder = post("/role/");
        assertThrows(NestedServletException.class, () -> {
            requestJson(requestBuilder, "{\"id\":1,\"roleName\":\"超级\",\"roleCode\":\"VVIP\"}", session);
        });
    }

    @Test
    void shouldRejectWhenUpdateContentWithoutId() throws Exception {
        requestJson(put("/role/2"), "{\"roleName\":\"超级\",\"roleCode\":\"VVIP\"}", session)
                .andExpect(status().is(400));

    }

    @Test
    void shouldRejectWhenPatchContentWithoutId() throws Exception {
        requestJson(patch("/role/2"), "{\"roleName\":\"超级\",\"roleCode\":\"VVIP\"}", session)
                .andExpect(status().is(400));
    }

}