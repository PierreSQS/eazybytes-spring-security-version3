package com.eazybytes.controller;

import com.eazybytes.model.Notice;
import com.eazybytes.repository.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Notices Controller Tests")
class NoticesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoticeRepository noticeRepository;

    private List<Notice> testNotices;

    @BeforeEach
    void setUp() {
        Notice notice1 = new Notice();
        notice1.setNoticeId(1L);
        notice1.setNoticeSummary("System Maintenance");
        notice1.setNoticeDetails("Our banking system will undergo scheduled maintenance on Saturday from 2 AM to 6 AM.");
        notice1.setNoticBegDt(Date.valueOf(LocalDate.now().minusDays(5)));
        notice1.setNoticEndDt(Date.valueOf(LocalDate.now().plusDays(5)));
        notice1.setCreateDt(LocalDateTime.now().minusDays(10));
        notice1.setUpdateDt(LocalDateTime.now().minusDays(5));

        Notice notice2 = new Notice();
        notice2.setNoticeId(2L);
        notice2.setNoticeSummary("New Feature Launch");
        notice2.setNoticeDetails("We're excited to announce the launch of our new mobile banking app!");
        notice2.setNoticBegDt(Date.valueOf(LocalDate.now().minusDays(3)));
        notice2.setNoticEndDt(Date.valueOf(LocalDate.now().plusDays(10)));
        notice2.setCreateDt(LocalDateTime.now().minusDays(7));
        notice2.setUpdateDt(LocalDateTime.now().minusDays(3));

        Notice notice3 = new Notice();
        notice3.setNoticeId(3L);
        notice3.setNoticeSummary("Holiday Hours");
        notice3.setNoticeDetails("Our branches will have special hours during the upcoming holiday season.");
        notice3.setNoticBegDt(Date.valueOf(LocalDate.now().minusDays(1)));
        notice3.setNoticEndDt(Date.valueOf(LocalDate.now().plusDays(15)));
        notice3.setCreateDt(LocalDateTime.now().minusDays(5));
        notice3.setUpdateDt(LocalDateTime.now().minusDays(1));

        testNotices = List.of(notice1, notice2, notice3);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should allow anonymous user to view notices")
    void testGetNotices_AnonymousUser_Success() throws Exception {
        when(noticeRepository.findAllActiveNotices()).thenReturn(testNotices);

        mockMvc.perform(get("/notices"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Cache-Control"))
                .andExpect(header().string("Cache-Control", "max-age=60"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].noticeId").value(1))
                .andExpect(jsonPath("$[0].noticeSummary").value("System Maintenance"))
                .andExpect(jsonPath("$[0].noticeDetails").value("Our banking system will undergo scheduled maintenance on Saturday from 2 AM to 6 AM."))
                .andExpect(jsonPath("$[1].noticeId").value(2))
                .andExpect(jsonPath("$[1].noticeSummary").value("New Feature Launch"))
                .andExpect(jsonPath("$[2].noticeId").value(3))
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should allow authenticated user to view notices")
    void testGetNotices_AuthenticatedUser_Success() throws Exception {
        when(noticeRepository.findAllActiveNotices()).thenReturn(testNotices);

        mockMvc.perform(get("/notices"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Cache-Control"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andDo(print());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should return cache control header with 60 seconds max-age")
    void testGetNotices_CacheControlHeader() throws Exception {
        when(noticeRepository.findAllActiveNotices()).thenReturn(testNotices);

        mockMvc.perform(get("/notices"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Cache-Control"))
                .andExpect(header().string("Cache-Control", "max-age=60"))
                .andDo(print());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should return empty list when no active notices found")
    void testGetNotices_NoActiveNotices() throws Exception {
        when(noticeRepository.findAllActiveNotices()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0))
                .andDo(print());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should return single notice correctly")
    void testGetNotices_SingleNotice() throws Exception {
        List<Notice> singleNotice = Collections.singletonList(testNotices.getFirst());
        when(noticeRepository.findAllActiveNotices()).thenReturn(singleNotice);

        mockMvc.perform(get("/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].noticeId").value(1))
                .andExpect(jsonPath("$[0].noticeSummary").value("System Maintenance"))
                .andDo(print());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should not include createDt and updateDt in response (JsonIgnore)")
    void testGetNotices_ExcludesIgnoredFields() throws Exception {
        when(noticeRepository.findAllActiveNotices()).thenReturn(Collections.singletonList(testNotices.getFirst()));

        mockMvc.perform(get("/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].createDt").doesNotExist())
                .andExpect(jsonPath("$[0].updateDt").doesNotExist())
                .andDo(print());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should include all required notice fields")
    void testGetNotices_IncludesAllRequiredFields() throws Exception {
        when(noticeRepository.findAllActiveNotices()).thenReturn(Collections.singletonList(testNotices.getFirst()));

        mockMvc.perform(get("/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].noticeId").exists())
                .andExpect(jsonPath("$[0].noticeSummary").exists())
                .andExpect(jsonPath("$[0].noticeDetails").exists())
                .andExpect(jsonPath("$[0].noticBegDt").exists())
                .andExpect(jsonPath("$[0].noticEndDt").exists())
                .andDo(print());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should handle multiple notices with different content")
    void testGetNotices_MultipleNoticesDifferentContent() throws Exception {
        when(noticeRepository.findAllActiveNotices()).thenReturn(testNotices);

        mockMvc.perform(get("/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].noticeSummary").value("System Maintenance"))
                .andExpect(jsonPath("$[1].noticeSummary").value("New Feature Launch"))
                .andExpect(jsonPath("$[2].noticeSummary").value("Holiday Hours"))
                .andDo(print());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Should verify notice details contain complete information")
    void testGetNotices_CompleteNoticeDetails() throws Exception {
        when(noticeRepository.findAllActiveNotices()).thenReturn(Collections.singletonList(testNotices.getFirst()));

        mockMvc.perform(get("/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].noticeDetails")
                        .value("Our banking system will undergo scheduled maintenance on Saturday from 2 AM to 6 AM."))
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = {"USER", "ADMIN"})
    @DisplayName("Should allow user with multiple roles to view notices")
    void testGetNotices_MultipleRoles_Success() throws Exception {
        when(noticeRepository.findAllActiveNotices()).thenReturn(testNotices);

        mockMvc.perform(get("/notices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andDo(print());
    }
}
