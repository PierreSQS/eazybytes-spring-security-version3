package com.eazybytes.controller;

import com.eazybytes.model.Contact;
import com.eazybytes.repository.ContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// quick junit test
class ContactControllerRCMockTest {

    private final ContactRepository contactRepository = mock(ContactRepository.class);

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient
                .bindToController(new ContactController(contactRepository))
                .build();

        Contact savedContact = new Contact();
        savedContact.setContactId("SR123456");
        savedContact.setContactName("Jane Doe");
        savedContact.setContactEmail("jane@example.com");
        savedContact.setSubject("Account Issue");
        savedContact.setMessage("I have a problem with my account.");

        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);
    }

    @Test
    void saveContactInquiry_withValidContact_returnsCreatedContacts() {
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

    // NOTE: The @PostFilter behavior cannot be tested with bindToController because the standalone MockMvc
    // setup has no Spring Security method-security infrastructure active. @PostFilter is simply ignored in
    // this mode, so a test asserting a filtered result would always fail. To cover that behavior, use a
    // @WebMvcTest / bindTo(mockMvc) test that imports the security configuration with @EnableMethodSecurity.
    @Test
    @Disabled("@PostFilter is not applied in bindToController tests, so this test will always fail. Use @WebMvcTest with security config to test filtering behavior.")
    void saveContactInquiry_withContactNameTest_returnsFilteredEmptyList() {
        // @PostFilter filters out contacts with contactName == 'Test'
        // The contact is saved but then filtered from the returned list
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

        Contact testContact = new Contact();
        testContact.setContactId("SR999999");
        testContact.setContactName("Test");
        testContact.setContactEmail("test@example.com");
        when(contactRepository.save(any(Contact.class))).thenReturn(testContact);

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
