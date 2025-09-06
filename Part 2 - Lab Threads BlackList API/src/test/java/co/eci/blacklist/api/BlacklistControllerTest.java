package co.eci.blacklist.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.eci.blacklist.BlacklistApiApplication;

@SpringBootTest(classes = BlacklistApiApplication.class)
@AutoConfigureMockMvc

/**
 * Test 5 - API tests for the BlacklistController endpoints.
 * Tests the REST API functionality and validation.
 *
 * @author ARSW-PANDILLA-2025
 * @version 1.0
 */
class BlacklistControllerTest {

    @Autowired
    MockMvc mockMvc;

    /**
     * Test 5.1: Tests the blacklist check endpoint with a valid IPv4 address.
     * Expects a 200 OK response.
     */
    @Test
    void test5_1_shouldReturn200ForValidIPv4() throws Exception {
        mockMvc.perform(get("/api/v1/blacklist/check")
                        .param("ip", "200.24.34.55")
                        .param("threads", "4"))
                .andExpect(status().isOk());
    }

    /**
     * Test 5.2: Tests the blacklist check endpoint with an invalid IPv4 address.
     * Expects a 400 Bad Request response.
     */
    @Test
    void test5_2_shouldReturn400ForInvalidIPv4() throws Exception {
        mockMvc.perform(get("/api/v1/blacklist/check")
                        .param("ip", "999.999.999.999"))
                .andExpect(status().isBadRequest());
    }
}
