package com.eazybytes.controller;

import com.eazybytes.model.Accounts;
import com.eazybytes.model.Customer;
import com.eazybytes.repository.AccountsRepository;
import com.eazybytes.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// quick junit test
class AccountControllerRCMockTest {

    private final AccountsRepository accountsRepository = mock(AccountsRepository.class);
    private final CustomerRepository customerRepository = mock(CustomerRepository.class);

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient
                .bindToController(new AccountController(accountsRepository, customerRepository))
                .build();

        Customer mockCustomer = Customer.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .mobileNumber("1111111111")
                .build();

        Accounts mockAccount = Accounts.builder()
                .customerId(1L)
                .accountNumber(123456789L)
                .accountType("Savings")
                .branchAddress("123 Main St")
                .build();

        when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockCustomer));
        when(accountsRepository.findByCustomerId(1L)).thenReturn(Optional.of(mockAccount));
        when(customerRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
    }

    @Test
    void getAccountDetails_withAuthenticatedUser_returnsAccount() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/myAccount").queryParam("email", "test@example.com").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accountNumber").isEqualTo(123456789)
                .jsonPath("$.accountType").isEqualTo("Savings");
    }

    @Test
    void getAccountDetails_withUnknownEmail_returnsEmptyBody() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/myAccount").queryParam("email", "unknown@example.com").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }
}
