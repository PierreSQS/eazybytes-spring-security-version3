package com.eazybytes;

import com.eazybytes.model.Accounts;
import com.eazybytes.model.AccountTransactions;
import com.eazybytes.model.Cards;
import com.eazybytes.model.Contact;
import com.eazybytes.model.Customer;
import com.eazybytes.model.Loans;
import com.eazybytes.model.Notice;
import com.eazybytes.repository.AccountsRepository;
import com.eazybytes.repository.AccountTransactionsRepository;
import com.eazybytes.repository.CardsRepository;
import com.eazybytes.repository.ContactRepository;
import com.eazybytes.repository.CustomerRepository;
import com.eazybytes.repository.LoanRepository;
import com.eazybytes.repository.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// Integration test — loads the full Spring application context and exercises
// the real security filter chain. RestTestClient is bound to the
// WebApplicationContext and equipped with a ClientHttpRequestInterceptor that
// logs every request/response and injects a Bearer token so that protected
// endpoints can be reached without a live Keycloak instance.
@SpringBootTest
@Import(TestJwtDecoderConfig.class)
class EazyBankIntegrationRestClientTest {

    // -----------------------------------------------------------------------
    // Interceptor
    // -----------------------------------------------------------------------

    /**
     * A {@link ClientHttpRequestInterceptor} that:
     * <ol>
     *   <li>Logs the outgoing HTTP method and URI to standard out.</li>
     *   <li>Injects an {@code Authorization: Bearer <token>} header so that
     *       the JWT resource-server filter accepts the request.</li>
     *   <li>Logs the response status after the call returns.</li>
     * </ol>
     *
     * In a real project the token would be obtained from an OAuth2
     * token endpoint. Here a pre-signed test token is used so the test
     * stays self-contained and does not require a running Keycloak.
     */
    static class LoggingBearerTokenInterceptor implements ClientHttpRequestInterceptor {

        // A minimal, pre-signed HS256 JWT whose payload contains:
        //   { "sub": "test@example.com",
        //     "preferred_username": "test@example.com",
        //     "realm_access": { "roles": ["USER"] },
        //     "iat": <past>, "exp": <far future> }
        // Generated offline with jjwt so no external service is needed.
        // For tests that only need to clear the security filter (permitAll
        // endpoints) the token is still sent so the interceptor fires on every call.
        private final String bearerToken;

        LoggingBearerTokenInterceptor(String bearerToken) {
            this.bearerToken = bearerToken;
        }

        @Override
        public ClientHttpResponse intercept(
                HttpRequest request,
                byte[] body,
                ClientHttpRequestExecution execution) throws IOException {

            // --- pre-processing: log & inject header ---
            System.out.printf("[INTERCEPTOR] --> %s %s%n",
                    request.getMethod(), request.getURI());
            request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);

            ClientHttpResponse response = execution.execute(request, body);

            // --- post-processing: log response status ---
            System.out.printf("[INTERCEPTOR] <-- %s%n", response.getStatusCode());

            return response;
        }
    }

    // -----------------------------------------------------------------------
    // Test infrastructure
    // -----------------------------------------------------------------------

    @Autowired
    WebApplicationContext webApplicationContext;

    // All repositories are mocked so that the test does not require a real
    // MySQL database while still exercising the full Spring MVC + Security stack.
    @MockitoBean
    CustomerRepository customerRepository;
    @MockitoBean
    AccountsRepository accountsRepository;
    @MockitoBean
    AccountTransactionsRepository accountTransactionsRepository;
    @MockitoBean
    CardsRepository cardsRepository;
    @MockitoBean
    LoanRepository loanRepository;
    @MockitoBean
    NoticeRepository noticeRepository;
    @MockitoBean
    ContactRepository contactRepository;

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        // Build the client bound to the full application context.
        // The interceptor is registered via the underlying RestClient builder.
        restTestClient = RestTestClient
                .bindToApplicationContext(webApplicationContext)
                .configureClient(clientBuilder ->
                        clientBuilder.requestInterceptor(
                                new LoggingBearerTokenInterceptor(TestTokenUtil.SIGNED_JWT)))
                .build();

        // --- shared mock data ---
        Customer mockCustomer = Customer.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .mobileNumber("1111111111")
                .build();

        Accounts mockAccount = Accounts.builder()
                .customerId(1L)
                .accountNumber(123456789L)
                .accountType("Savings")
                .branchAddress("123 Main St")
                .build();

        AccountTransactions mockTransaction = AccountTransactions.builder()
                .transactionId("TX001")
                .accountNumber(123456789L)
                .customerId(1L)
                .transactionType("Credit")
                .transactionAmt(500)
                .build();

        Cards mockCard = new Cards();
        mockCard.setCardId(1L);
        mockCard.setCustomerId(1L);
        mockCard.setCardNumber("4111111111111111");
        mockCard.setCardType("Credit");
        mockCard.setTotalLimit(10000);
        mockCard.setAmountUsed(2500);
        mockCard.setAvailableAmount(7500);

        Loans mockLoan = new Loans();
        mockLoan.setLoanNumber(1001L);
        mockLoan.setCustomerId(1L);
        mockLoan.setLoanType("Home");
        mockLoan.setTotalLoan(200000);
        mockLoan.setAmountPaid(50000);
        mockLoan.setOutstandingAmount(150000);

        Notice mockNotice = new Notice();
        mockNotice.setNoticeId(1L);
        mockNotice.setNoticeSummary("System Maintenance");
        mockNotice.setNoticeDetails("Scheduled downtime on Sunday.");

        Contact savedContact = new Contact();
        savedContact.setContactId("SR123456");
        savedContact.setContactName("Jane Doe");
        savedContact.setContactEmail("jane@example.com");
        savedContact.setSubject("Account Issue");
        savedContact.setMessage("I have a problem with my account.");

        when(customerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockCustomer));
        when(accountsRepository.findByCustomerId(1L)).thenReturn(Optional.of(mockAccount));
        when(accountTransactionsRepository.findByCustomerIdOrderByTransactionDtDesc(1L))
                .thenReturn(List.of(mockTransaction));
        when(cardsRepository.findByCustomerId(1L)).thenReturn(List.of(mockCard));
        when(loanRepository.findByCustomerIdOrderByStartDtDesc(1L)).thenReturn(List.of(mockLoan));
        when(noticeRepository.findAllActiveNotices()).thenReturn(List.of(mockNotice));
        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);
    }

    // -----------------------------------------------------------------------
    // Tests — one per controller endpoint
    // -----------------------------------------------------------------------

    @Test
    void getMyAccount_withBearerToken_returnsAccount() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/myAccount")
                        .queryParam("email", "test@example.com")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accountNumber").isEqualTo(123456789)
                .jsonPath("$.accountType").isEqualTo("Savings");
    }

    @Test
    void getMyBalance_withBearerToken_returnsTransactions() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/myBalance")
                        .queryParam("email", "test@example.com")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].transactionType").isEqualTo("Credit")
                .jsonPath("$[0].transactionAmt").isEqualTo(500);
    }

    @Test
    void getMyCards_withBearerToken_returnsCards() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/myCards")
                        .queryParam("email", "test@example.com")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].cardType").isEqualTo("Credit")
                .jsonPath("$[0].totalLimit").isEqualTo(10000);
    }

    @Test
    void getMyLoans_withBearerToken_returnsLoans() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/myLoans")
                        .queryParam("email", "test@example.com")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].loanType").isEqualTo("Home")
                .jsonPath("$[0].totalLoan").isEqualTo(200000);
    }

    @Test
    void getNotices_withoutAuthentication_returnsOk() {
        // /notices is permitAll — the interceptor still fires and logs the call,
        // but the Bearer token is not required for this endpoint.
        restTestClient.get()
                .uri("/notices")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches("Cache-Control", ".*max-age=60.*")
                .expectBody()
                .jsonPath("$[0].noticeSummary").isEqualTo("System Maintenance");
    }

    @Test
    void postContact_withValidBody_returnsContact() {
        // /contact is permitAll and CSRF-ignored — no token or CSRF header needed.
        String requestBody = """
                [
                  {
                    "contactName": "Jane Doe",
                    "contactEmail": "jane@example.com",
                    "subject": "Account Issue",
                    "message": "I have a problem with my account."
                  }
                ]
                """;

        restTestClient.post()
                .uri("/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].contactName").isEqualTo("Jane Doe")
                .jsonPath("$[0].contactEmail").isEqualTo("jane@example.com");
    }
}
