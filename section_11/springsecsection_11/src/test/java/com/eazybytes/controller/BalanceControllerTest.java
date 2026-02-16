package com.eazybytes.controller;

import com.eazybytes.model.AccountTransactions;
import com.eazybytes.repository.AccountTransactionsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BalanceController.class)
@DisplayName("Balance Controller Tests")
class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountTransactionsRepository accountTransactionsRepository;

    private List<AccountTransactions> testTransactions;

    @BeforeEach
    void setUp() {
        AccountTransactions transaction1 = new AccountTransactions();
        transaction1.setTransactionId("TXN001");
        transaction1.setCustomerId(1L);
        transaction1.setAccountNumber(123456789L);
        transaction1.setTransactionDt(Date.valueOf(LocalDate.now().minusDays(1)));
        transaction1.setTransactionSummary("Salary Credit");
        transaction1.setTransactionType("Credit");
        transaction1.setTransactionAmt(5000);
        transaction1.setClosingBalance(15000);
        transaction1.setCreateDt(LocalDateTime.now());

        AccountTransactions transaction2 = new AccountTransactions();
        transaction2.setTransactionId("TXN002");
        transaction2.setCustomerId(1L);
        transaction2.setAccountNumber(123456789L);
        transaction2.setTransactionDt(Date.valueOf(LocalDate.now().minusDays(2)));
        transaction2.setTransactionSummary("Utility Payment");
        transaction2.setTransactionType("Debit");
        transaction2.setTransactionAmt(150);
        transaction2.setClosingBalance(10000);
        transaction2.setCreateDt(LocalDateTime.now());

        testTransactions = Arrays.asList(transaction1, transaction2);
    }

    @Test
    @DisplayName("Should return 401 when user is not authenticated")
    void testGetBalanceDetails_Unauthorized() throws Exception {
        mockMvc.perform(get("/myBalance")
                        .param("id", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return balance details for authenticated user with USER role")
    void testGetBalanceDetails_WithUserRole_Success() throws Exception {
        when(accountTransactionsRepository.findByCustomerIdOrderByTransactionDtDesc(1L))
                .thenReturn(testTransactions);

        mockMvc.perform(get("/myBalance")
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].transactionId").value("TXN001"))
                .andExpect(jsonPath("$[0].transactionSummary").value("Salary Credit"))
                .andExpect(jsonPath("$[0].transactionType").value("Credit"))
                .andExpect(jsonPath("$[0].transactionAmt").value(5000))
                .andExpect(jsonPath("$[1].transactionId").value("TXN002"))
                .andExpect(jsonPath("$[1].transactionType").value("Debit"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return balance details for authenticated user with ADMIN role")
    void testGetBalanceDetails_WithAdminRole_Success() throws Exception {
        when(accountTransactionsRepository.findByCustomerIdOrderByTransactionDtDesc(1L))
                .thenReturn(testTransactions);

        mockMvc.perform(get("/myBalance")
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("Should return 403 for GUEST role (requires USER or ADMIN)")
    void testGetBalanceDetails_WithGuestRole_Forbidden() throws Exception {
        mockMvc.perform(get("/myBalance")
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return empty list when no transactions found")
    void testGetBalanceDetails_NoTransactionsFound() throws Exception {
        when(accountTransactionsRepository.findByCustomerIdOrderByTransactionDtDesc(anyLong()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/myBalance")
                        .param("id", "999")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should handle single transaction correctly")
    void testGetBalanceDetails_SingleTransaction() throws Exception {
        List<AccountTransactions> singleTransaction = Collections.singletonList(testTransactions.getFirst());
        when(accountTransactionsRepository.findByCustomerIdOrderByTransactionDtDesc(1L))
                .thenReturn(singleTransaction);

        mockMvc.perform(get("/myBalance")
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].transactionId").value("TXN001"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should verify transactions are ordered by date descending")
    void testGetBalanceDetails_OrderedByDateDesc() throws Exception {
        when(accountTransactionsRepository.findByCustomerIdOrderByTransactionDtDesc(1L))
                .thenReturn(testTransactions);

        mockMvc.perform(get("/myBalance")
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId").value("TXN001"))
                .andExpect(jsonPath("$[1].transactionId").value("TXN002"));
    }
}
