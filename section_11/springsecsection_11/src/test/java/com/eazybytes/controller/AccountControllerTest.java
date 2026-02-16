package com.eazybytes.controller;

import com.eazybytes.model.Accounts;
import com.eazybytes.repository.AccountsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@DisplayName("Account Controller Tests")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountsRepository accountsRepository;

    private Accounts testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Accounts();
        testAccount.setCustomerId(1L);
        testAccount.setAccountNumber(123456789L);
        testAccount.setAccountType("Savings");
        testAccount.setBranchAddress("123 Main Street, New York");
        testAccount.setCreateDt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should return 401 when user is not authenticated")
    void testGetAccountDetails_Unauthorized() throws Exception {
        mockMvc.perform(get("/myAccount")
                        .param("id", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return account details for authenticated user with USER role")
    void testGetAccountDetails_WithUserRole_Success() throws Exception {
        when(accountsRepository.findByCustomerId(1L)).thenReturn(testAccount);

        mockMvc.perform(get("/myAccount")
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.accountNumber").value(123456789))
                .andExpect(jsonPath("$.accountType").value("Savings"))
                .andExpect(jsonPath("$.branchAddress").value("123 Main Street, New York"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 403 for ADMIN role (requires USER role)")
    void testGetAccountDetails_WithAdminRole_Forbidden() throws Exception {
        mockMvc.perform(get("/myAccount")
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return null when account not found")
    void testGetAccountDetails_AccountNotFound() throws Exception {
        when(accountsRepository.findByCustomerId(anyLong())).thenReturn(null);

        mockMvc.perform(get("/myAccount")
                        .param("id", "999")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should handle different customer IDs correctly")
    void testGetAccountDetails_DifferentCustomerId() throws Exception {
        Accounts anotherAccount = new Accounts();
        anotherAccount.setCustomerId(2L);
        anotherAccount.setAccountNumber(987654321L);
        anotherAccount.setAccountType("Checking");
        anotherAccount.setBranchAddress("456 Oak Avenue, Boston");
        anotherAccount.setCreateDt(LocalDateTime.now());

        when(accountsRepository.findByCustomerId(2L)).thenReturn(anotherAccount);

        mockMvc.perform(get("/myAccount")
                        .param("id", "2")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(2))
                .andExpect(jsonPath("$.accountNumber").value(987654321))
                .andExpect(jsonPath("$.accountType").value("Checking"));
    }
}
