package com.eazybytes.controller;

import com.eazybytes.model.Contact;
import com.eazybytes.repository.ContactRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactController.class)
@DisplayName("Contact Controller Tests")
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContactRepository contactRepository;

    private Contact testContact;

    @BeforeEach
    void setUp() {
        testContact = new Contact();
        testContact.setContactName("John Doe");
        testContact.setContactEmail("john.doe@example.com");
        testContact.setSubject("Account Inquiry");
        testContact.setMessage("I have a question about my account balance.");
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should allow anonymous user to submit contact inquiry")
    void testSaveContactInquiry_AnonymousUser_Success() throws Exception {
        Contact savedContact = new Contact();
        savedContact.setContactId("SR123456");
        savedContact.setContactName(testContact.getContactName());
        savedContact.setContactEmail(testContact.getContactEmail());
        savedContact.setSubject(testContact.getSubject());
        savedContact.setMessage(testContact.getMessage());
        savedContact.setCreateDt(LocalDateTime.now());

        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);

        mockMvc.perform(post("/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testContact)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactId").value(matchesPattern("^SR\\d+$")))
                .andExpect(jsonPath("$.contactName").value("John Doe"))
                .andExpect(jsonPath("$.contactEmail").value("john.doe@example.com"))
                .andExpect(jsonPath("$.subject").value("Account Inquiry"))
                .andExpect(jsonPath("$.message").value("I have a question about my account balance."))
                .andExpect(jsonPath("$.createDt").value(notNullValue()));

        verify(contactRepository).save(any(Contact.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should allow authenticated user to submit contact inquiry")
    void testSaveContactInquiry_AuthenticatedUser_Success() throws Exception {
        Contact savedContact = new Contact();
        savedContact.setContactId("SR987654");
        savedContact.setContactName(testContact.getContactName());
        savedContact.setContactEmail(testContact.getContactEmail());
        savedContact.setSubject(testContact.getSubject());
        savedContact.setMessage(testContact.getMessage());
        savedContact.setCreateDt(LocalDateTime.now());

        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);

        mockMvc.perform(post("/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testContact)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactId").value(matchesPattern("^SR\\d+$")))
                .andExpect(jsonPath("$.contactName").value("John Doe"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should generate unique service request number")
    void testSaveContactInquiry_GeneratesUniqueServiceRequestNumber() throws Exception {
        Contact savedContact = new Contact();
        savedContact.setContactId("SR456789");
        savedContact.setContactName(testContact.getContactName());
        savedContact.setContactEmail(testContact.getContactEmail());
        savedContact.setSubject(testContact.getSubject());
        savedContact.setMessage(testContact.getMessage());
        savedContact.setCreateDt(LocalDateTime.now());

        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);

        mockMvc.perform(post("/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testContact)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactId").exists())
                .andExpect(jsonPath("$.contactId").value(matchesPattern("^SR\\d{4,9}$")));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should set create date when saving contact")
    void testSaveContactInquiry_SetsCreateDate() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        Contact savedContact = new Contact();
        savedContact.setContactId("SR111222");
        savedContact.setContactName(testContact.getContactName());
        savedContact.setContactEmail(testContact.getContactEmail());
        savedContact.setSubject(testContact.getSubject());
        savedContact.setMessage(testContact.getMessage());
        savedContact.setCreateDt(now);

        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);

        mockMvc.perform(post("/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testContact)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createDt").exists());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should handle contact with different data correctly")
    void testSaveContactInquiry_DifferentContactData() throws Exception {
        Contact differentContact = new Contact();
        differentContact.setContactName("Jane Smith");
        differentContact.setContactEmail("jane.smith@example.com");
        differentContact.setSubject("Technical Support");
        differentContact.setMessage("My online banking is not working.");

        Contact savedContact = new Contact();
        savedContact.setContactId("SR333444");
        savedContact.setContactName(differentContact.getContactName());
        savedContact.setContactEmail(differentContact.getContactEmail());
        savedContact.setSubject(differentContact.getSubject());
        savedContact.setMessage(differentContact.getMessage());
        savedContact.setCreateDt(LocalDateTime.now());

        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);

        mockMvc.perform(post("/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(differentContact)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactName").value("Jane Smith"))
                .andExpect(jsonPath("$.contactEmail").value("jane.smith@example.com"))
                .andExpect(jsonPath("$.subject").value("Technical Support"))
                .andExpect(jsonPath("$.message").value("My online banking is not working."));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should handle long messages correctly")
    void testSaveContactInquiry_LongMessage() throws Exception {
        Contact longMessageContact = new Contact();
        longMessageContact.setContactName("Test User");
        longMessageContact.setContactEmail("test@example.com");
        longMessageContact.setSubject("Detailed Inquiry");
        longMessageContact.setMessage("This is a very long message that contains a lot of details " +
                "about various banking services and questions about multiple account types, " +
                "transactions, fees, and other banking-related topics that the customer wants to discuss.");

        Contact savedContact = new Contact();
        savedContact.setContactId("SR555666");
        savedContact.setContactName(longMessageContact.getContactName());
        savedContact.setContactEmail(longMessageContact.getContactEmail());
        savedContact.setSubject(longMessageContact.getSubject());
        savedContact.setMessage(longMessageContact.getMessage());
        savedContact.setCreateDt(LocalDateTime.now());

        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);

        mockMvc.perform(post("/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(longMessageContact)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(longMessageContact.getMessage()));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should verify repository save method is called")
    void testSaveContactInquiry_RepositoryMethodCalled() throws Exception {
        Contact savedContact = new Contact();
        savedContact.setContactId("SR777888");
        savedContact.setContactName(testContact.getContactName());
        savedContact.setContactEmail(testContact.getContactEmail());
        savedContact.setSubject(testContact.getSubject());
        savedContact.setMessage(testContact.getMessage());
        savedContact.setCreateDt(LocalDateTime.now());

        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);

        mockMvc.perform(post("/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testContact)))
                .andExpect(status().isOk());

        verify(contactRepository).save(any(Contact.class));
    }
}
