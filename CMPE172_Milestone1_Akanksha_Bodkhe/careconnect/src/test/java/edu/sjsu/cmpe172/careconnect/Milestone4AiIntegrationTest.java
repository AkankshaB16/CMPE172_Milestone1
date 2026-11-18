package edu.sjsu.cmpe172.careconnect;

import edu.sjsu.cmpe172.careconnect.ai.AppointmentAiTools;
import edu.sjsu.cmpe172.careconnect.notification.NotificationGateway;
import edu.sjsu.cmpe172.careconnect.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class Milestone4AiIntegrationTest {

    @Autowired
    private AppointmentAiTools appointmentTools;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private NotificationGateway notificationGateway;

    @Test
    void aiBookingRequiresConfirmationAndBlocksDoubleBooking() {

        String preview = appointmentTools.bookAppointment(
                1L,
                3L,
                false
        );

        assertEquals(
                true,
                preview.startsWith("BOOKING_PREVIEW:")
        );

        assertEquals(
                0,
                countBookedAppointments(3L)
        );

        appointmentTools.bookAppointment(
                1L,
                3L,
                true
        );

        assertEquals(
                1,
                countBookedAppointments(3L)
        );

        assertThrows(
                BookingService.SlotConflictException.class,
                () -> appointmentTools.bookAppointment(
                        2L,
                        3L,
                        true
                )
        );

        assertEquals(
                1,
                countBookedAppointments(3L)
        );
    }

    private int countBookedAppointments(Long slotId) {
        return jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM appointments
                WHERE slot_id = ?
                  AND status = 'BOOKED'
                """,
                Integer.class,
                slotId
        );
    }
}