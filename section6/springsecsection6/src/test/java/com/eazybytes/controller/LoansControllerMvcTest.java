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

@WebMvcTest(LoansController.class)
@Import(ProjectSecurityConfig.class)
class LoansControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient.bindTo(mockMvc).build();
    }

    @Test
    @DisplayName("GET /myLoans - unauthenticated - should return 401 Unauthorized")
    void getLoansDetails_unauthenticated_shouldReturnUnauthorized() {
        restTestClient.get().uri("/myLoans")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser
    @DisplayName("GET /myLoans - authenticated - should return loans details")
    void getLoansDetails_authenticated_shouldReturnOk() {
        restTestClient.get().uri("/myLoans")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Here are the loans details from the DB");
    }
}
