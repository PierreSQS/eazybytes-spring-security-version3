package com.eazybytes.controller;

import com.eazybytes.model.Cards;
import com.eazybytes.model.Customer;
import com.eazybytes.repository.CardsRepository;
import com.eazybytes.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// quick junit test
class CardsControllerRCMockTest {

    private final CardsRepository cardsRepository = mock(CardsRepository.class);
    private final CustomerRepository customerRepository = mock(CustomerRepository.class);

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient
                .bindToController(new CardsController(cardsRepository, customerRepository))
                .build();

        Customer mockCustomer = Customer.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .mobileNumber("1111111111")
                .build();

        Cards mockCard = new Cards();
        mockCard.setCardId(1L);
        mockCard.setCustomerId(1L);
        mockCard.setCardNumber("4111111111111111");
        mockCard.setCardType("Credit");
        mockCard.setTotalLimit(10000);
        mockCard.setAmountUsed(2500);
        mockCard.setAvailableAmount(7500);

        when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockCustomer));
        when(cardsRepository.findByCustomerId(1L)).thenReturn(List.of(mockCard));
        when(customerRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
    }

    @Test
    void getCardDetails_withAuthenticatedUser_returnsCards() {
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
    void getCardDetails_withUnknownEmail_returnsEmptyBody() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/myCards").queryParam("email", "unknown@example.com").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }
}
