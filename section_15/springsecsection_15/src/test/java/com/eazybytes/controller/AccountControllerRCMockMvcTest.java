package com.eazybytes.controller;

import com.eazybytes.config.ProjectSecurityConfig;
import com.eazybytes.model.Accounts;
import com.eazybytes.model.Customer;
import com.eazybytes.repository.AccountsRepository;
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

import java.util.Optional;

import static org.mockito.Mockito.when;

// Test the Controller and Spring MVC
@WebMvcTest(AccountController.class)
@Import(ProjectSecurityConfig.class)
class AccountControllerRCMockMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AccountsRepository accountsRepository;

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
        when(accountsRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(Accounts.builder()
                        .customerId(1L)
                        .accountNumber(123456789L)
                        .accountType("Savings")
                        .branchAddress("123 Main St")
                        .build()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAccountDetails_withUserRole_returnsAccount() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/myAccount").queryParam("email", "test@example.com").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accountNumber").isEqualTo(123456789)
                .jsonPath("$.accountType").isEqualTo("Savings");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAccountDetails_withAdminRole_returnsForbidden() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/myAccount").queryParam("email", "test@example.com").build())
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void getAccountDetails_withoutAuthentication_returnsUnauthorized() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/myAccount").queryParam("email", "test@example.com").build())
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
