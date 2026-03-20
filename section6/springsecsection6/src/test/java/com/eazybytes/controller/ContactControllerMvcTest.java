package com.eazybytes.controller;

import com.eazybytes.config.ProjectSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(ContactController.class)
@Import(ProjectSecurityConfig.class)
class ContactControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient.bindTo(mockMvc).build();
    }

    @Test
    @DisplayName("GET /contact - should return inquiry saved message without authentication")
    void saveContactInquiryDetails_shouldReturnOk() {
        restTestClient.get().uri("/contact")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Inquiry details are saved to the DB");
    }
}
