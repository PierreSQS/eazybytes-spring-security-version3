package com.eazybytes;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;

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
        //   { "sub": "happy@example.com",
        //     "preferred_username": "happy@example.com",
        //     "realm_access": { "roles": ["USER"] },
        //     "iat": <past>, "exp": <far future> }
        // Generated offline with JJWT so no external service is needed.
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

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        // Build the client bound to the full application context.
        // The interceptor is registered via the underlying RestClient builder.
        restTestClient = RestTestClient
                .bindToApplicationContext(webApplicationContext)
                .requestInterceptor(
                                new LoggingBearerTokenInterceptor(TestTokenUtil.SIGNED_JWT))
                .build();

    }

    // -----------------------------------------------------------------------
    // Tests — one per controller endpoint
    // -----------------------------------------------------------------------

    @Test
    void getMyAccount_withBearerToken_returnsAccount() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/myAccount")
                        .queryParam("email", "happy@example.com")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accountNumber").isEqualTo(1865764534)
                .jsonPath("$.accountType").isEqualTo("Savings");
    }

    @Test
    void getMyBalance_withBearerToken_returnsTransactions() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/myBalance")
                        .queryParam("email", "happy@example.com")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].transactionType").isEqualTo("Withdrawal")
                .jsonPath("$[0].transactionAmt").isEqualTo(100);
    }

    @Test
    void getMyCards_withBearerToken_returnsCards() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/myCards")
                        .queryParam("email", "happy@example.com")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].cardType").isEqualTo("Credit")
                .jsonPath("$[0].totalLimit").isEqualTo(10000);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getMyLoans_withBearerToken_returnsLoans() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/myLoans")
                        .queryParam("email", "happy@example.com")
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
                .jsonPath("$[0].noticeSummary").isEqualTo("Home Loan Interest rates reduced");
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
