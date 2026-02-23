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

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(ProjectSecurityConfig.class)
class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AccountsRepository accountsRepository;

    @MockitoBean
    CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        // Mock-Daten vorbereiten
        when(customerRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(Customer.builder()
                        .id(1L)
                        .name("Test")
                        .email("test@example.com")
                        .mobileNumber("1111111111")
                        .build( )));
        when(accountsRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(Accounts.builder()
                        .customerId(1L)
                        .accountNumber(123456789L)
                        .accountType("Savings")
                        .build()));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetAccountDetailsWithValidUser() throws Exception {
        mockMvc.perform(get("/myAccount").param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("123456789"))
                .andExpect(jsonPath("$.accountType").value("Savings"));
    }

    @Test
    void testGetAccountDetailsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/myAccount").param("email", "test@example.com"))
                .andExpect(status().isUnauthorized());
    }
}