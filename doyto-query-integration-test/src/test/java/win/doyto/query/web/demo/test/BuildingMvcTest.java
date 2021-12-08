package win.doyto.query.web.demo.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * BuildingMvcTest
 *
 * @author f0rb on 2021-12-08
 */
class BuildingMvcTest extends DemoApplicationTest {

    @BeforeEach
    void setUp() throws Exception {
        String data = "[{\"name\": \"Times Building\"}]";
        performAndExpectSuccess(buildJson(post("/building/"), data));
    }

    @Test
    void getBuilding() throws Exception {
        performAndExpectSuccess(get("/building/"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].id").exists())
                .andExpect(jsonPath("$.data.list[0].name").value("Times Building"))
        ;
    }
}
