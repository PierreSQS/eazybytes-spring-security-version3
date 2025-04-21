package com.eazybytes.controller;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureMockMvc
class UserControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @SpringMock
    private CustomerRepository customerRepository;

    @SpringMock
    private PasswordEncoder passwordEncoder;

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