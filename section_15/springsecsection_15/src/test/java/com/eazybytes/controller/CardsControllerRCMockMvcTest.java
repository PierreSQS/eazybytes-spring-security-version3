package com.eazybytes.controller;

import com.eazybytes.config.ProjectSecurityConfig;
import com.eazybytes.model.Cards;
import com.eazybytes.model.Customer;
import com.eazybytes.repository.CardsRepository;
import com.eazybytes.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

// Test the Controller and Spring MVC
@WebMvcTest(CardsController.class)
@Import(ProjectSecurityConfig.class)
class CardsControllerRCMockMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CardsRepository cardsRepository;

    @MockitoBean
    CustomerRepository customerRepository;

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient.bindTo(mockMvc).build();

        Cards mockCard = new Cards();
        mockCard.setCardId(1L);
        mockCard.setCustomerId(1L);
        mockCard.setCardNumber("4111111111111111");
        mockCard.setCardType("Credit");
        mockCard.setTotalLimit(10000);
        mockCard.setAmountUsed(2500);
        mockCard.setAvailableAmount(7500);

        when(customerRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(Customer.builder()
                        .id(1L)
                        .name("Test User")
                        .email("test@example.com")
                        .mobileNumber("1111111111")
                        .build()));
        when(cardsRepository.findByCustomerId(1L)).thenReturn(List.of(mockCard));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCardDetails_withUserRole_returnsCards() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/myCards").queryParam("email", "test@example.com").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].cardType").isEqualTo("Credit")
                .jsonPath("$[0].totalLimit").isEqualTo(10000)
                .jsonPath("$[0].availableAmount").isEqualTo(7500);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCardDetails_withAdminRole_returnsForbidden() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/myCards").queryParam("email", "test@example.com").build())
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void getCardDetails_withoutAuthentication_returnsUnauthorized() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/myCards").queryParam("email", "test@example.com").build())
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
