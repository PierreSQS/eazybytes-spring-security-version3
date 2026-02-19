package com.eazybytes.controller;

import com.eazybytes.config.ProjectSecurityConfig;
import com.eazybytes.model.Contact;
import com.eazybytes.repository.ContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// Test the Controller and Spring MVC
// @EnableMethodSecurity is required here so that @PostFilter on ContactController is enforced
// during the web-slice test. Without it the filter is silently skipped.
@WebMvcTest(ContactController.class)
@Import(ProjectSecurityConfig.class)
@EnableMethodSecurity
class ContactControllerRCMockMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ContactRepository contactRepository;

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient.bindTo(mockMvc).build();

        Contact savedContact = new Contact();
        savedContact.setContactId("SR123456");
        savedContact.setContactName("Jane Doe");
        savedContact.setContactEmail("jane@example.com");
        savedContact.setSubject("Account Issue");
        savedContact.setMessage("I have a problem with my account.");

        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);
    }

    @Test
    void saveContactInquiry_withValidContact_returnsContact() {
        // /contact is permitAll — no authentication needed
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

    @Test
    @WithMockUser
    void saveContactInquiry_withContactNameTest_isFilteredByPostFilter() {
        // @PostFilter("filterObject.contactName != 'Test'") removes the entry from the returned list.
        Contact testContact = new Contact();
        testContact.setContactId("SR999999");
        testContact.setContactName("Test");
        testContact.setContactEmail("test@example.com");
        when(contactRepository.save(any(Contact.class))).thenReturn(testContact);

        String requestBody = """
                [
                  {
                    "contactName": "Test",
                    "contactEmail": "test@example.com",
                    "subject": "Filter Me",
                    "message": "This contact should be filtered by @PostFilter."
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
                .jsonPath("$").isEmpty();
    }

    @Test
    void saveContactInquiry_withEmptyList_returnsEmptyList() {
        restTestClient.post()
                .uri("/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .body("[]")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isEmpty();
    }
}
