package com.eazybytes.controller;

import com.eazybytes.config.ProjectSecurityConfig;
import com.eazybytes.model.Notice;
import com.eazybytes.repository.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

import static org.mockito.Mockito.when;

// Test the Controller and Spring MVC
@WebMvcTest(NoticesController.class)
@Import(ProjectSecurityConfig.class)
class NoticesControllerRCMockMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    NoticeRepository noticeRepository;

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient.bindTo(mockMvc).build();

        Notice mockNotice = new Notice();
        mockNotice.setNoticeId(1L);
        mockNotice.setNoticeSummary("System Maintenance");
        mockNotice.setNoticeDetails("The system will be down for maintenance on Sunday.");

        when(noticeRepository.findAllActiveNotices()).thenReturn(List.of(mockNotice));
    }

    @Test
    void getNotices_withoutAuthentication_returnsOk() {
        // /notices is permitAll — no authentication required
        restTestClient.get()
                .uri("/notices")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches("Cache-Control", ".*max-age=60.*")
                .expectBody()
                .jsonPath("$[0].noticeSummary").isEqualTo("System Maintenance")
                .jsonPath("$[0].noticeDetails").isEqualTo("The system will be down for maintenance on Sunday.");
    }

    @Test
    void getNotices_whenRepositoryReturnsNull_returnsNullBody() {
        when(noticeRepository.findAllActiveNotices()).thenReturn(null);

        restTestClient.get()
                .uri("/notices")
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }
}
