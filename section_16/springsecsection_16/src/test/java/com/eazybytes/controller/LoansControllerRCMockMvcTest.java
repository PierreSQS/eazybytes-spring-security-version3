package com.eazybytes.controller;

import com.eazybytes.config.ProjectSecurityConfig;
import com.eazybytes.model.Customer;
import com.eazybytes.model.Loans;
import com.eazybytes.repository.CustomerRepository;
import com.eazybytes.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

// Test the Controller and Spring MVC
// @EnableMethodSecurity is required here so that @PostAuthorize("hasRole('USER')") on
// LoansController is enforced during the web-slice test. Without it the annotation is skipped.
@WebMvcTest(LoansController.class)
@Import(ProjectSecurityConfig.class)
@EnableMethodSecurity
class LoansControllerRCMockMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    LoanRepository loanRepository;

    @MockitoBean
    CustomerRepository customerRepository;

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient.bindTo(mockMvc).build();

        Loans mockLoan = new Loans();
        mockLoan.setLoanNumber(1001L);
        mockLoan.setCustomerId(1L);
        mockLoan.setLoanType("Home");
        mockLoan.setTotalLoan(200000);
        mockLoan.setAmountPaid(50000);
        mockLoan.setOutstandingAmount(150000);

        when(customerRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(Customer.builder()
                        .id(1L)
                        .name("Test User")
                        .email("test@example.com")
                        .mobileNumber("1111111111")
                        .build()));
        when(loanRepository.findByCustomerIdOrderByStartDtDesc(1L)).thenReturn(List.of(mockLoan));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getLoanDetails_withUserRole_returnsLoans() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/myLoans").queryParam("email", "test@example.com").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].loanType").isEqualTo("Home")
                .jsonPath("$[0].totalLoan").isEqualTo(200000)
                .jsonPath("$[0].outstandingAmount").isEqualTo(150000);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getLoanDetails_withAdminRole_returnsForbidden() {
        // /myLoans is authenticated() at the URL level, but @PostAuthorize requires ROLE_USER.
        // An ADMIN without ROLE_USER will be denied after the method executes.
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/myLoans").queryParam("email", "test@example.com").build())
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void getLoanDetails_withoutAuthentication_returnsUnauthorized() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/myLoans").queryParam("email", "test@example.com").build())
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
