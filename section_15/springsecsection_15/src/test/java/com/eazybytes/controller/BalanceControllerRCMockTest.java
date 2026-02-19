package com.eazybytes.controller;

import com.eazybytes.model.AccountTransactions;
import com.eazybytes.model.Customer;
import com.eazybytes.repository.AccountTransactionsRepository;
import com.eazybytes.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// quick junit test
class BalanceControllerRCMockTest {

    private final AccountTransactionsRepository accountTransactionsRepository = mock(AccountTransactionsRepository.class);
    private final CustomerRepository customerRepository = mock(CustomerRepository.class);

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient
                .bindToController(new BalanceController(accountTransactionsRepository, customerRepository))
                .build();

        Customer mockCustomer = Customer.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .mobileNumber("1111111111")
                .build();

        AccountTransactions mockTransaction = AccountTransactions.builder()
                .transactionId("TX001")
                .accountNumber(123456789L)
                .customerId(1L)
                .transactionType("Credit")
                .transactionAmt(500)
                .build();

        when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockCustomer));
        when(accountTransactionsRepository.findByCustomerIdOrderByTransactionDtDesc(1L))
                .thenReturn(List.of(mockTransaction));
        when(customerRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
    }

    @Test
    void getBalanceDetails_withAuthenticatedUser_returnsTransactions() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/myBalance").queryParam("email", "test@example.com").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].transactionType").isEqualTo("Credit")
                .jsonPath("$[0].transactionAmt").isEqualTo(500);
    }

    @Test
    void getBalanceDetails_withUnknownEmail_returnsEmptyBody() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/myBalance").queryParam("email", "unknown@example.com").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }
}
