package com.eazybytes.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import com.eazybytes.config.ProjectSecurityConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(AccountController.class)
@Import(ProjectSecurityConfig.class)
class AccountControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient.bindTo(mockMvc).build();
    }

    @Test
    @DisplayName("GET /myAccount - unauthenticated - should return 401 Unauthorized")
    void getAccountDetails_unauthenticated_shouldReturnUnauthorized() {
        restTestClient.get().uri("/myAccount")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser
    @DisplayName("GET /myAccount - authenticated - should return account details")
    void getAccountDetails_authenticated_shouldReturnOk() {
        restTestClient.get().uri("/myAccount")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Here are the account details from the DB");
    }
}

