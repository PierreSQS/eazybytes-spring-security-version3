package com.eazybytes.controller;

import com.eazybytes.config.ProjectSecurityConfig;
import com.eazybytes.model.Customer;
import com.eazybytes.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@WebMvcTest(UserController.class)
@Import(ProjectSecurityConfig.class)
class UserControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerRepository customerRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient.bindTo(mockMvc).build();
    }

    @Test
    @DisplayName("POST /register - successful registration - should return 201 CREATED")
    void registerUser_success_shouldReturnCreated() {
        Customer customer = Customer.builder()
                .email("test@example.com")
                .pwd("password123")
                .role("ROLE_USER")
                .build();

        Customer savedCustomer = Customer.builder()
                .id(1L)
                .email("test@example.com")
                .pwd("encodedPassword")
                .role("ROLE_USER")
                .build();

        given(passwordEncoder.encode("password123")).willReturn("encodedPassword");
        given(customerRepository.save(any(Customer.class))).willReturn(savedCustomer);

        restTestClient.post().uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(customer))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .isEqualTo("Given user details are successfully registered");
    }

    @Test
    @DisplayName("POST /register - save returns id 0 - should return 400 BAD_REQUEST")
    void registerUser_failedSave_shouldReturnBadRequest() {
        Customer customer = Customer.builder()
                .email("fail@example.com")
                .pwd("password123")
                .role("ROLE_USER")
                .build();

        Customer savedCustomer = Customer.builder()
                .id(0L)
                .email("fail@example.com")
                .pwd("encodedPassword")
                .role("ROLE_USER")
                .build();

        given(passwordEncoder.encode("password123")).willReturn("encodedPassword");
        given(customerRepository.save(any(Customer.class))).willReturn(savedCustomer);

        restTestClient.post().uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(customer))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("User registration failed");
    }

    @Test
    @DisplayName("POST /register - exception thrown - should return 500 INTERNAL_SERVER_ERROR")
    void registerUser_exception_shouldReturnInternalServerError() {
        Customer customer = Customer.builder()
                .email("error@example.com")
                .pwd("password123")
                .role("ROLE_USER")
                .build();

        given(passwordEncoder.encode("password123")).willReturn("encodedPassword");
        given(customerRepository.save(any(Customer.class)))
                .willThrow(new RuntimeException("DB connection lost"));

        restTestClient.post().uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(customer))
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("An exception occurred: DB connection lost"));
    }
}
