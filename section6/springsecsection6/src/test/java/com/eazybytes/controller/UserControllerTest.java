package com.eazybytes.controller;


import com.eazybytes.model.Customer;
import com.eazybytes.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CustomerRepository customerRepository;

    @MockitoBean
    PasswordEncoder passwordEncoder;

    @Test
    void registerUserReturnsCreatedWhenValidCustomerProvided() throws Exception {
        Customer customer = new Customer();
        customer.setPwd("plainPassword");

        given(passwordEncoder.encode("plainPassword")).willReturn("hashedPassword");
        given(customerRepository.save(any(Customer.class))).willAnswer(invocation -> {
            Customer savedCustomer = invocation.getArgument(0);
            savedCustomer.setId(1L);
            return savedCustomer;
        });

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pwd\":\"plainPassword\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().string("Given user details are successfully registered"));
    }

    @Test
    void registerUserReturnsBadRequestWhenSaveFails() throws Exception {
        given(passwordEncoder.encode("plainPassword")).willReturn("hashedPassword");
        given(customerRepository.save(any(Customer.class))).willReturn(null);

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pwd\":\"plainPassword\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User registration failed"));
    }

    @Test
    void registerUserReturnsInternalServerErrorOnException() throws Exception {
        given(passwordEncoder.encode("plainPassword")).willReturn("hashedPassword");
        given(customerRepository.save(any(Customer.class))).willThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pwd\":\"plainPassword\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("An exception occurred: Database error")));
    }
}