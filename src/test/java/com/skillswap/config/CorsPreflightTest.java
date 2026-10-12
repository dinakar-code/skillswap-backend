package com.skillswap.config;

import com.skillswap.SkillSwapApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = SkillSwapApplication.class,
        properties = "FRONTEND_URL=https://skillswap-frontend-sand.vercel.app"
)
@AutoConfigureMockMvc
class CorsPreflightTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginPreflightAllowsVercelFrontend() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "https://skillswap-frontend-sand.vercel.app")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin",
                        "https://skillswap-frontend-sand.vercel.app"))
                .andExpect(header().string("Access-Control-Allow-Methods",
                        allOf(
                                containsString("GET"),
                                containsString("POST"),
                                containsString("PUT"),
                                containsString("PATCH"),
                                containsString("DELETE"),
                                containsString("OPTIONS"))))
                .andExpect(header().string("Access-Control-Allow-Headers",
                        allOf(containsString("authorization"), containsString("content-type"))))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
    }
}
