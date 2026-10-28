package edu.sjsu.cmpe172.careconnect;

import edu.sjsu.cmpe172.careconnect.notification.NotificationGateway;
import edu.sjsu.cmpe172.careconnect.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest
class Milestone3IntegrationTest {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private NotificationGateway notificationGateway;

    @Test
    void bookingStillSucceedsWhenNotificationServiceIsDown() {
        doThrow(new RuntimeException("notification service unavailable"))
                .when(notificationGateway)
                .send(org.mockito.ArgumentMatchers.any());

        assertDoesNotThrow(() -> bookingService.book(1L, 1L));

        Integer appointments = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM appointments WHERE slot_id = 1 AND customer_id = 1 AND status = 'BOOKED'",
                Integer.class
        );
        assertEquals(1, appointments);
        verify(notificationGateway).send(org.mockito.ArgumentMatchers.any());
    }
}
