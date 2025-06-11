package com.eazybytes.controller;

import com.eazybytes.model.Customer;
import com.eazybytes.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.core.env.Environment;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CustomerRepository customerRepository;

    @MockitoBean
    PasswordEncoder passwordEncoder;

    @MockitoBean
    AuthenticationManager authenticationManager;

    Environment env;

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


        mockMvc.perform(post("/apiLogin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void getUserDetails_unauthorized() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().isUnauthorized());
    }

}