package com.eazybytes.controller;

import com.eazybytes.model.Customer;
import com.eazybytes.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.core.env.Environment;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(com.eazybytes.config.ProjectSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private Environment env;

    @Test
    void registerUser_success() throws Exception {
        Customer customer = new Customer();
        customer.setId(1);
        customer.setEmail("test@example.com");
        customer.setPwd("password");
        customer.setCreateDt(LocalDateTime.now());

        Mockito.when(passwordEncoder.encode(Mockito.anyString())).thenReturn("hashedPwd");
        Mockito.when(customerRepository.save(Mockito.any(Customer.class))).thenReturn(customer);

        String json = """
                {
                  "email": "test@example.com",
                  "pwd": "password"
                }
                """;

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string("Given user details are successfully registered"));
    }

    @Test
    void loginUser_success() throws Exception {
        // Beispiel für einen Login-Test, falls vorhanden
        String json = """
                {
                  "email": "test@example.com",
                  "pwd": "password"
                }
                """;

        // Hier ggf. AuthenticationManager und weitere Mocks konfigurieren

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void getUserDetails_unauthorized() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().isUnauthorized());
    }

    // Weitere Tests für alle Handler-Methoden ergänzen, z.B.:
    // - getUserDetails_authenticated
    // - updateUser
    // - deleteUser
    // Die Implementierung hängt von den Methoden im UserController ab.
}