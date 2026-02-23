package com.eazybytes.controller;

import com.eazybytes.model.Customer;
import com.eazybytes.model.Loans;
import com.eazybytes.repository.CustomerRepository;
import com.eazybytes.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// quick junit test
class LoansControllerRCMockTest {

    private final LoanRepository loanRepository = mock(LoanRepository.class);
    private final CustomerRepository customerRepository = mock(CustomerRepository.class);

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient
                .bindToController(new LoansController(loanRepository, customerRepository))
                .build();

        Customer mockCustomer = Customer.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .mobileNumber("1111111111")
                .build();

        Loans mockLoan = new Loans();
        mockLoan.setLoanNumber(1001L);
        mockLoan.setCustomerId(1L);
        mockLoan.setLoanType("Home");
        mockLoan.setTotalLoan(200000);
        mockLoan.setAmountPaid(50000);
        mockLoan.setOutstandingAmount(150000);

        when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockCustomer));
        when(loanRepository.findByCustomerIdOrderByStartDtDesc(1L)).thenReturn(List.of(mockLoan));
        when(customerRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
    }

    @Test
    void getLoanDetails_withAuthenticatedUser_returnsLoans() {
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
    void getLoanDetails_withUnknownEmail_returnsEmptyBody() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/myLoans").queryParam("email", "unknown@example.com").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }
}
