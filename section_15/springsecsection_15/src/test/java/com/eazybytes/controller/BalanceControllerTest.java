package com.eazybytes.controller;

import com.eazybytes.config.ProjectSecurityConfig;
import com.eazybytes.model.AccountTransactions;
import com.eazybytes.model.Customer;
import com.eazybytes.repository.AccountTransactionsRepository;
import com.eazybytes.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BalanceController.class)
@Import(ProjectSecurityConfig.class)
class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountTransactionsRepository accountTransactionsRepository;

    @MockBean
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        // Mock-Daten vorbereiten
        Customer mockCustomer = new Customer(1L, "Test", "User", "test@example.com");
        AccountTransactions mockTransaction = new AccountTransactions(1L, 1L, "Credit", 100.0, "2023-01-01");

        when(customerRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(mockCustomer));
        when(accountTransactionsRepository.findByCustomerIdOrderByTransactionDtDesc(1L))
                .thenReturn(List.of(mockTransaction));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetBalanceDetailsWithValidUser() throws Exception {
        mockMvc.perform(get("/myBalance").param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionType").value("Credit"))
                .andExpect(jsonPath("$[0].amount").value(100.0));
    }

    @Test
    void testGetBalanceDetailsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/myBalance").param("email", "test@example.com"))
                .andExpect(status().isUnauthorized());
    }
}