package edu.sjsu.cmpe172.careconnect.service;

import edu.sjsu.cmpe172.careconnect.dto.NotificationRequest;
import edu.sjsu.cmpe172.careconnect.repository.AppointmentRepository;
import edu.sjsu.cmpe172.careconnect.repository.SlotRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class BookingTransactionService {
    private final SlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;

    public BookingTransactionService(
            SlotRepository slotRepository,
            AppointmentRepository appointmentRepository
    ) {
        this.slotRepository = slotRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public NotificationRequest book(Long customerId, Long slotId) {
        Map<String, Object> slot = slotRepository.findSlot(slotId);
        if (slot.isEmpty()) {
            throw new IllegalArgumentException("Slot not found");
        }

        int currentVersion = ((Number) slot.get("VERSION")).intValue();
        int claimed = slotRepository.claimSlotOptimistically(slotId, currentVersion);
        if (claimed != 1) {
            throw new BookingService.SlotConflictException(
                    "This slot was booked by another customer"
            );
        }

        try {
            appointmentRepository.create(slotId, customerId);
        } catch (DuplicateKeyException exception) {
            throw new BookingService.SlotConflictException(
                    "This slot was booked by another customer"
            );
        }

        return new NotificationRequest(
                "customer-" + customerId,
                "BOOKING_CONFIRMATION",
                "Appointment booked successfully for slot " + slotId
        );
    }

    @Transactional
    public NotificationRequest cancel(Long customerId, Long appointmentId) {
        int updated = appointmentRepository.cancelOwned(appointmentId, customerId);
        if (updated != 1) {
            throw new IllegalArgumentException(
                    "Appointment not found, already completed, or it does not belong to you"
            );
        }

        return new NotificationRequest(
                "customer-" + customerId,
                "CANCELLATION_CONFIRMATION",
                "Appointment cancelled successfully: " + appointmentId
        );
    }
}
