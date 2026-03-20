package com.eazybytes.controller;

import com.eazybytes.config.ProjectSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(BalanceController.class)
@Import(ProjectSecurityConfig.class)
class BalanceControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient.bindTo(mockMvc).build();
    }

    @Test
    @DisplayName("GET /myBalance - unauthenticated - should return 401 Unauthorized")
    void getBalanceDetails_unauthenticated_shouldReturnUnauthorized() {
        restTestClient.get().uri("/myBalance")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser
    @DisplayName("GET /myBalance - authenticated - should return balance details")
    void getBalanceDetails_authenticated_shouldReturnOk() {
        restTestClient.get().uri("/myBalance")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Here are the balance details from the DB");
    }
}
