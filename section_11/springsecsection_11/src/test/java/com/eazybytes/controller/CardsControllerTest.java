package com.eazybytes.controller;

import com.eazybytes.model.Cards;
import com.eazybytes.repository.CardsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Cards Controller Tests")
class CardsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardsRepository cardsRepository;

    private List<Cards> testCards;

    @BeforeEach
    void setUp() {
        Cards card1 = new Cards();
        card1.setCardId(1L);
        card1.setCustomerId(1L);
        card1.setCardNumber("4111111111111111");
        card1.setCardType("Credit Card");
        card1.setTotalLimit(50000);
        card1.setAmountUsed(15000);
        card1.setAvailableAmount(35000);
        card1.setCreateDt(LocalDateTime.now());

        Cards card2 = new Cards();
        card2.setCardId(2L);
        card2.setCustomerId(1L);
        card2.setCardNumber("5555555555554444");
        card2.setCardType("Debit Card");
        card2.setTotalLimit(25000);
        card2.setAmountUsed(5000);
        card2.setAvailableAmount(20000);
        card2.setCreateDt(LocalDateTime.now());

        testCards = List.of(card1, card2);
    }

    @Test
    @DisplayName("Should return 401 when user is not authenticated")
    void testGetCardDetails_Unauthorized() throws Exception {
        mockMvc.perform(get("/myCards")
                        .param("id", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return card details for authenticated user with USER role")
    void testGetCardDetails_WithUserRole_Success() throws Exception {
        when(cardsRepository.findByCustomerId(1L)).thenReturn(testCards);

        mockMvc.perform(get("/myCards")
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].cardId").value(1))
                .andExpect(jsonPath("$[0].cardNumber").value("4111111111111111"))
                .andExpect(jsonPath("$[0].cardType").value("Credit Card"))
                .andExpect(jsonPath("$[0].totalLimit").value(50000))
                .andExpect(jsonPath("$[0].amountUsed").value(15000))
                .andExpect(jsonPath("$[0].availableAmount").value(35000))
                .andExpect(jsonPath("$[1].cardId").value(2))
                .andExpect(jsonPath("$[1].cardType").value("Debit Card"))
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 403 for ADMIN role (requires USER role)")
    void testGetCardDetails_WithAdminRole_Forbidden() throws Exception {
        mockMvc.perform(get("/myCards")
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return empty list when no cards found")
    void testGetCardDetails_NoCardsFound() throws Exception {
        when(cardsRepository.findByCustomerId(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/myCards")
                        .param("id", "999")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should handle single card correctly")
    void testGetCardDetails_SingleCard() throws Exception {
        List<Cards> singleCard = Collections.singletonList(testCards.getFirst());
        when(cardsRepository.findByCustomerId(1L)).thenReturn(singleCard);

        mockMvc.perform(get("/myCards")
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cardId").value(1))
                .andExpect(jsonPath("$[0].cardType").value("Credit Card"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should handle different customer IDs correctly")
    void testGetCardDetails_DifferentCustomerId() throws Exception {
        Cards card = new Cards();
        card.setCardId(3L);
        card.setCustomerId(2L);
        card.setCardNumber("6011111111111117");
        card.setCardType("Platinum Card");
        card.setTotalLimit(100000);
        card.setAmountUsed(30000);
        card.setAvailableAmount(70000);
        card.setCreateDt(LocalDateTime.now());

        when(cardsRepository.findByCustomerId(2L)).thenReturn(Collections.singletonList(card));

        mockMvc.perform(get("/myCards")
                        .param("id", "2")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cardId").value(3))
                .andExpect(jsonPath("$[0].customerId").value(2))
                .andExpect(jsonPath("$[0].cardType").value("Platinum Card"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should verify all card details are correctly mapped")
    void testGetCardDetails_VerifyAllFields() throws Exception {
        when(cardsRepository.findByCustomerId(1L)).thenReturn(Collections.singletonList(testCards.getFirst()));

        mockMvc.perform(get("/myCards")
                        .param("id", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cardId").exists())
                .andExpect(jsonPath("$[0].customerId").exists())
                .andExpect(jsonPath("$[0].cardNumber").exists())
                .andExpect(jsonPath("$[0].cardType").exists())
                .andExpect(jsonPath("$[0].totalLimit").exists())
                .andExpect(jsonPath("$[0].amountUsed").exists())
                .andExpect(jsonPath("$[0].availableAmount").exists());
    }
}
