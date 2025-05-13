package com.eazybytes.controller;

import com.eazybytes.config.ProjectSecurityConfig;
import com.eazybytes.model.Accounts;
import com.eazybytes.repository.AccountsRepository;
import com.eazybytes.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(ProjectSecurityConfig.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountsRepository accountsRepository;

    @MockBean
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        // Mock-Daten vorbereiten
        when(customerRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(new Customer(1L, "Test", "User", "test@example.com")));
        when(accountsRepository.findByCustomerId(1L))
                .thenReturn(Optional.of(new Accounts(1L, "123456789", "Savings", 1L)));
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