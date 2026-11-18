package edu.sjsu.cmpe172.careconnect.ai;

import edu.sjsu.cmpe172.careconnect.dto.AvailableSlotDto;
import edu.sjsu.cmpe172.careconnect.model.AppointmentView;
import edu.sjsu.cmpe172.careconnect.service.BookingService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Component
public class AppointmentAiTools {

    private final BookingService bookingService;
    private final ThreadLocal<Long> loggedInCustomer = new ThreadLocal<>();

    public AppointmentAiTools(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    public <T> T asLoggedInCustomer(Long customerId, Supplier<T> action) {
        loggedInCustomer.set(customerId);

        try {
            return action.get();
        } finally {
            loggedInCustomer.remove();
        }
    }

    @Tool(description = "Find available healthcare appointment slots.")
    public List<AvailableSlotDto> checkAvailability(
            Long serviceId,
            String date
    ) {
        return bookingService.browse(
                null,
                serviceId,
                date,
                0,
                20
        );
    }

    @Tool(description = "List the logged-in patient's appointments.")
    public List<AppointmentView> listMyAppointments(
            Long customerId
    ) {
        return bookingService.customerAppointments(
                effectiveCustomer(customerId)
        );
    }

    @Tool(description = "Prepare or commit a booking. Always use confirm=false first. Only book after explicit confirmation.")
    public String bookAppointment(
            Long customerId,
            Long slotId,
            boolean confirm
    ) {
        if (confirm) {
            bookingService.book(
                    effectiveCustomer(customerId),
                    slotId
            );

            return "BOOKED: appointment for slot " + slotId;
        }

        AvailableSlotDto slot = bookingService
                .browse(null, null, null, 0, 100)
                .stream()
                .filter(candidate -> candidate.slotId().equals(slotId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "That slot is no longer available."
                        )
                );

        return "BOOKING_PREVIEW: "
                + slot.serviceName()
                + " with "
                + slot.providerName()
                + " on "
                + slot.startTime()
                + ". Ask the patient to confirm before booking.";
    }

    @Tool(description = "Cancel one of the logged-in patient's appointments.")
    public String cancelAppointment(
            Long customerId,
            Long appointmentId
    ) {
        bookingService.cancel(
                effectiveCustomer(customerId),
                appointmentId
        );

        return "CANCELLED: appointment " + appointmentId;
    }

    private Long effectiveCustomer(Long requestedCustomerId) {
        Long loggedInId = loggedInCustomer.get();

        return loggedInId == null
                ? requestedCustomerId
                : loggedInId;
    }
}