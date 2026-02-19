package com.eazybytes.controller;

import com.eazybytes.config.ProjectSecurityConfig;
import com.eazybytes.model.AccountTransactions;
import com.eazybytes.model.Customer;
import com.eazybytes.repository.AccountTransactionsRepository;
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
@WebMvcTest(BalanceController.class)
@Import(ProjectSecurityConfig.class)
class BalanceControllerRCMockMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AccountTransactionsRepository accountTransactionsRepository;

    @MockitoBean
    CustomerRepository customerRepository;

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient.bindTo(mockMvc).build();

        when(customerRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(Customer.builder()
                        .id(1L)
                        .name("Test User")
                        .email("test@example.com")
                        .mobileNumber("1111111111")
                        .build()));
        when(accountTransactionsRepository.findByCustomerIdOrderByTransactionDtDesc(1L))
                .thenReturn(List.of(AccountTransactions.builder()
                        .transactionId("TX001")
                        .accountNumber(123456789L)
                        .customerId(1L)
                        .transactionType("Credit")
                        .transactionAmt(500)
                        .build()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getBalanceDetails_withUserRole_returnsTransactions() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/myBalance").queryParam("email", "test@example.com").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].transactionType").isEqualTo("Credit")
                .jsonPath("$[0].transactionAmt").isEqualTo(500);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBalanceDetails_withAdminRole_returnsTransactions() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/myBalance").queryParam("email", "test@example.com").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].transactionType").isEqualTo("Credit");
    }

    @Test
    void getBalanceDetails_withoutAuthentication_returnsUnauthorized() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/myBalance").queryParam("email", "test@example.com").build())
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
