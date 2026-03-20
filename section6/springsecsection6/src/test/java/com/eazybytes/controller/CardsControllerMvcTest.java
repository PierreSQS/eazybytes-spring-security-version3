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

@WebMvcTest(CardsController.class)
@Import(ProjectSecurityConfig.class)
class CardsControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient.bindTo(mockMvc).build();
    }

    @Test
    @DisplayName("GET /myCards - unauthenticated - should return 401 Unauthorized")
    void getCardsDetails_unauthenticated_shouldReturnUnauthorized() {
        restTestClient.get().uri("/myCards")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser
    @DisplayName("GET /myCards - authenticated - should return card details")
    void getCardsDetails_authenticated_shouldReturnOk() {
        restTestClient.get().uri("/myCards")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Here are the card details from the DB");
    }
}
