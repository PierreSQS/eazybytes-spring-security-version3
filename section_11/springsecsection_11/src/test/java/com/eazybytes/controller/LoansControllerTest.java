package com.eazybytes.controller;

import com.eazybytes.model.Loans;
import com.eazybytes.repository.LoanRepository;
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

@WebMvcTest(LoansController.class)
@DisplayName("Loans Controller Tests")
class LoansControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanRepository loanRepository;

    private List<Loans> testLoans;

    @BeforeEach
    void setUp() {
        Loans loan1 = new Loans();
        loan1.setLoanNumber(100001L);
        loan1.setCustomerId(1L);
        loan1.setStartDt(Date.valueOf(LocalDate.now().minusYears(1)));
        loan1.setLoanType("Home Loan");
        loan1.setTotalLoan(500000);
        loan1.setAmountPaid(100000);
        loan1.setOutstandingAmount(400000);
        loan1.setCreateDt(LocalDateTime.now());

        Loans loan2 = new Loans();
        loan2.setLoanNumber(100002L);
        loan2.setCustomerId(1L);
        loan2.setStartDt(Date.valueOf(LocalDate.now().minusYears(2)));
        loan2.setLoanType("Car Loan");
        loan2.setTotalLoan(150000);
        loan2.setAmountPaid(75000);
        loan2.setOutstandingAmount(75000);
        loan2.setCreateDt(LocalDateTime.now());

        testLoans = Arrays.asList(loan1, loan2);
    }

    @Test
    @DisplayName("Should return 401 when user is not authenticated")
    void testGetLoanDetails_Unauthorized() throws Exception {
        mockMvc.perform(get("/myLoans")
                        .param("id", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return loan details for authenticated user with USER role")
    void testGetLoanDetails_WithUserRole_Success() throws Exception {
        when(loanRepository.findByCustomerIdOrderByStartDtDesc(1L)).thenReturn(testLoans);

        mockMvc.perform(get("/myLoans")
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].loanNumber").value(100001))
                .andExpect(jsonPath("$[0].loanType").value("Home Loan"))
                .andExpect(jsonPath("$[0].totalLoan").value(500000))
                .andExpect(jsonPath("$[0].amountPaid").value(100000))
                .andExpect(jsonPath("$[0].outstandingAmount").value(400000))
                .andExpect(jsonPath("$[1].loanNumber").value(100002))
                .andExpect(jsonPath("$[1].loanType").value("Car Loan"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 403 for ADMIN role (requires USER role)")
    void testGetLoanDetails_WithAdminRole_Forbidden() throws Exception {
        mockMvc.perform(get("/myLoans")
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return empty list when no loans found")
    void testGetLoanDetails_NoLoansFound() throws Exception {
        when(loanRepository.findByCustomerIdOrderByStartDtDesc(anyLong()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/myLoans")
                        .param("id", "999")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should handle single loan correctly")
    void testGetLoanDetails_SingleLoan() throws Exception {
        List<Loans> singleLoan = Collections.singletonList(testLoans.getFirst());
        when(loanRepository.findByCustomerIdOrderByStartDtDesc(1L)).thenReturn(singleLoan);

        mockMvc.perform(get("/myLoans")
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].loanNumber").value(100001))
                .andExpect(jsonPath("$[0].loanType").value("Home Loan"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should handle different customer IDs correctly")
    void testGetLoanDetails_DifferentCustomerId() throws Exception {
        Loans loan = new Loans();
        loan.setLoanNumber(100003L);
        loan.setCustomerId(2L);
        loan.setStartDt(Date.valueOf(LocalDate.now().minusMonths(6)));
        loan.setLoanType("Personal Loan");
        loan.setTotalLoan(50000);
        loan.setAmountPaid(10000);
        loan.setOutstandingAmount(40000);
        loan.setCreateDt(LocalDateTime.now());

        when(loanRepository.findByCustomerIdOrderByStartDtDesc(2L))
                .thenReturn(Collections.singletonList(loan));

        mockMvc.perform(get("/myLoans")
                        .param("id", "2")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].loanNumber").value(100003))
                .andExpect(jsonPath("$[0].customerId").value(2))
                .andExpect(jsonPath("$[0].loanType").value("Personal Loan"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should verify loans are ordered by start date descending")
    void testGetLoanDetails_OrderedByStartDateDesc() throws Exception {
        when(loanRepository.findByCustomerIdOrderByStartDtDesc(1L)).thenReturn(testLoans);

        mockMvc.perform(get("/myLoans")
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].loanNumber").value(100001))
                .andExpect(jsonPath("$[1].loanNumber").value(100002));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should verify all loan details are correctly mapped")
    void testGetLoanDetails_VerifyAllFields() throws Exception {
        when(loanRepository.findByCustomerIdOrderByStartDtDesc(1L))
                .thenReturn(Collections.singletonList(testLoans.getFirst()));

        mockMvc.perform(get("/myLoans")
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].loanNumber").exists())
                .andExpect(jsonPath("$[0].customerId").exists())
                .andExpect(jsonPath("$[0].startDt").exists())
                .andExpect(jsonPath("$[0].loanType").exists())
                .andExpect(jsonPath("$[0].totalLoan").exists())
                .andExpect(jsonPath("$[0].amountPaid").exists())
                .andExpect(jsonPath("$[0].outstandingAmount").exists());
    }
}
