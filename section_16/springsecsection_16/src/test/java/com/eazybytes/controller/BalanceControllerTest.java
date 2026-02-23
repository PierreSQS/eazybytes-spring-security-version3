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

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BalanceController.class)
@Import(ProjectSecurityConfig.class)
class BalanceControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AccountTransactionsRepository accountTransactionsRepository;

    @MockitoBean
    CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        // Mock-Daten vorbereiten
        Customer mockCustomer = Customer.builder()
                .id(1L)
                .name("Test")
                .email("test@example.com")
                .mobileNumber("1111111111")
                .build();
        AccountTransactions mockTransaction = AccountTransactions.builder()
                .transactionId("1L")
                .accountNumber(123456789L)
                .customerId(1L)
                .transactionType("Credit")
                .transactionAmt(100)
                .build();

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
                .andExpect(jsonPath("$[0].transactionAmt").value(100.0))
                .andDo(print());
    }

    @Test
    void testGetBalanceDetailsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/myBalance").param("email", "test@example.com"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
}