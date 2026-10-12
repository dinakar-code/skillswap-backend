package com.skillswap.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillswap.SkillSwapApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = SkillSwapApplication.class,
        properties = "FRONTEND_URL=https://skillswap-frontend-gamma.vercel.app"
)
@AutoConfigureMockMvc
class SkillSwapApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginPreflightAllowsVercelFrontend() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "https://skillswap-frontend-gamma.vercel.app")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin",
                        "https://skillswap-frontend-gamma.vercel.app"))
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

    @Test
    void dashboardProfileContainsSkillNamesRatherThanSkillObjects() throws Exception {
        String token = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"alice@skillswap.dev","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String bearerToken = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(token)
                .get("token")
                .asText();

        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", "Bearer " + bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.teachingSkills[0]").isString())
                .andExpect(jsonPath("$.profile.learningSkills[0]").isString());
    }

    @Test
    void registrationPersistsSubmittedTeachingAndLearningSkills() throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"New Student",
                                  "email":"new-student@skillswap.dev",
                                  "password":"password123",
                                  "teachingSkills":["Java"],
                                  "learningSkills":["Python"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.teachingSkills[0]").value("Java"))
                .andExpect(jsonPath("$.user.learningSkills[0]").value("Python"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String token = objectMapper.readTree(response).get("token").asText();

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teachingSkills[0]").value("Java"))
                .andExpect(jsonPath("$.learningSkills[0]").value("Python"));
    }

    @Test
    void exchangeRequestCanBeCreatedWithTimestampAndPendingStatus() throws Exception {
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"alice@skillswap.dev","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String token = objectMapper.readTree(loginResponse).get("token").asText();
        String studentsResponse = mockMvc.perform(get("/api/users/search")
                        .header("Authorization", "Bearer " + token)
                        .param("query", "Bob"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long receiverId = objectMapper.readTree(studentsResponse).get(0).get("id").asLong();

        mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "receiverId", receiverId,
                                "wantedSkill", "Python",
                                "offeredSkill", "Java",
                                "message", "Let's exchange skills."
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void acceptedExchangeChatCanLoadAndSendMessagesOnlyForParticipants() throws Exception {
        String aliceToken = loginToken("alice@skillswap.dev");
        String bobToken = loginToken("bob@skillswap.dev");
        String carolToken = loginToken("carol@skillswap.dev");
        long bobId = objectMapper.readTree(mockMvc.perform(get("/api/users/search")
                        .header("Authorization", "Bearer " + aliceToken)
                        .param("query", "Bob"))
                .andReturn().getResponse().getContentAsString()).get(0).get("id").asLong();
        String requestResponse = mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "receiverId", bobId,
                                "wantedSkill", "Python",
                                "offeredSkill", "Java",
                                "message", "Let's exchange skills."
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long requestId = objectMapper.readTree(requestResponse).get("id").asLong();

        mockMvc.perform(patch("/api/requests/{id}/respond", requestId)
                        .header("Authorization", "Bearer " + bobToken)
                        .param("action", "accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        mockMvc.perform(post("/api/requests/{id}/messages", requestId)
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"Hi Bob, ready to plan our first session?"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderName").value("Alice Johnson"));

        mockMvc.perform(get("/api/requests/{id}/messages", requestId)
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].content").value("Hi Bob, ready to plan our first session?"));

        mockMvc.perform(get("/api/requests/{id}/messages", requestId)
                        .header("Authorization", "Bearer " + carolToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void profileCanBeDeactivatedAndReactivatedWithoutLosingAccount() throws Exception {
        String email = "visibility-check@skillswap.dev";
        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "name", "Visibility Check",
                                "email", email,
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(registerResponse).get("token").asText();
        String aliceToken = loginToken("alice@skillswap.dev");

        mockMvc.perform(patch("/api/users/me/deactivate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
        mockMvc.perform(get("/api/users/search")
                        .header("Authorization", "Bearer " + aliceToken)
                        .param("query", "Visibility Check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(patch("/api/users/me/activate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
        mockMvc.perform(get("/api/users/search")
                        .header("Authorization", "Bearer " + aliceToken)
                        .param("query", "Visibility Check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void permanentProfileDeletionRemovesAccountAndExchangeChat() throws Exception {
        String email = "delete-check@skillswap.dev";
        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "name", "Delete Check",
                                "email", email,
                                "password", "password123",
                                "teachingSkills", java.util.List.of("Java"),
                                "learningSkills", java.util.List.of("Python")
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String userToken = objectMapper.readTree(registerResponse).get("token").asText();
        String bobToken = loginToken("bob@skillswap.dev");
        long bobId = objectMapper.readTree(mockMvc.perform(get("/api/users/search")
                        .header("Authorization", "Bearer " + userToken)
                        .param("query", "Bob"))
                .andReturn().getResponse().getContentAsString()).get(0).get("id").asLong();
        String requestResponse = mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "receiverId", bobId,
                                "wantedSkill", "Python",
                                "offeredSkill", "Java",
                                "message", "Let's exchange skills."
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long requestId = objectMapper.readTree(requestResponse).get("id").asLong();
        mockMvc.perform(patch("/api/requests/{id}/respond", requestId)
                        .header("Authorization", "Bearer " + bobToken)
                        .param("action", "accept"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/requests/{id}/messages", requestId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"This chat will be deleted with my profile."}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/requests/{id}/messages", requestId)
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "email", email,
                                "password", "password123"
                        ))))
                .andExpect(status().isForbidden());
    }

    private String loginToken(String email) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "email", email,
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }
}
