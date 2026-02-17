package com.eazybytes.controller;

import com.eazybytes.model.Contact;
import com.eazybytes.repository.ContactRepository;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Contact Controller Tests")
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
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
                .andExpect(jsonPath("$.message").value("My online banking is not working."))
                .andDo(print());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should handle long messages correctly")
    void testSaveContactInquiry_LongMessage() throws Exception {
        Contact contact = getContact();

        Contact savedContact = new Contact();
        savedContact.setContactId("SR555666");
        savedContact.setContactName(contact.getContactName());
        savedContact.setContactEmail(contact.getContactEmail());
        savedContact.setSubject(contact.getSubject());
        savedContact.setMessage(contact.getMessage());
        savedContact.setCreateDt(LocalDateTime.now());

        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);

        mockMvc.perform(post("/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(contact)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(contact.getMessage()));
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

    private static @NonNull Contact getContact() {
        Contact contact = new Contact();
        contact.setContactName("Test User");
        contact.setContactEmail("test@example.com");
        contact.setSubject("Detailed Inquiry");
        contact.setMessage("This is a very long message that contains a lot of details " +
                "about various banking services and questions about multiple account types, " +
                "transactions, fees, and other banking-related topics that the customer wants to discuss.");
        return contact;
    }

}
