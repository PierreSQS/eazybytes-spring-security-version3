package com.eazybytes.controller;

import com.eazybytes.model.Notice;
import com.eazybytes.repository.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// quick junit test
class NoticesControllerRCMockTest {

    private final NoticeRepository noticeRepository = mock(NoticeRepository.class);

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient
                .bindToController(new NoticesController(noticeRepository))
                .build();

        Notice mockNotice = new Notice();
        mockNotice.setNoticeId(1L);
        mockNotice.setNoticeSummary("System Maintenance");
        mockNotice.setNoticeDetails("The system will be down for maintenance on Sunday.");

        when(noticeRepository.findAllActiveNotices()).thenReturn(List.of(mockNotice));
    }

    @Test
    void getNotices_withActiveNotices_returnsOkWithCacheControl() {
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
    void getNotices_withNoActiveNotices_returnsNullBody() {
        when(noticeRepository.findAllActiveNotices()).thenReturn(null);

        restTestClient.get()
                .uri("/notices")
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }
}
