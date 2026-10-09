package edu.sjsu.cmpe172.careconnect.service;

import edu.sjsu.cmpe172.careconnect.dto.AvailableSlotDto;
import edu.sjsu.cmpe172.careconnect.dto.SlotForm;
import edu.sjsu.cmpe172.careconnect.model.AppointmentView;
import edu.sjsu.cmpe172.careconnect.repository.AppointmentRepository;
import edu.sjsu.cmpe172.careconnect.repository.SlotRepository;
import edu.sjsu.cmpe172.careconnect.repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class BookingService {

    private final SlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public BookingService(
            SlotRepository slotRepository,
            AppointmentRepository appointmentRepository,
            UserRepository userRepository
    ) {
        this.slotRepository = slotRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    public List<AvailableSlotDto> browse(
            Long providerId,
            Long serviceId,
            String date,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        return slotRepository.findAvailableSlots(
                providerId,
                serviceId,
                date,
                safePage,
                safeSize
        );
    }

    @Transactional
    public void book(Long customerId, Long slotId) {

        Map<String, Object> slot = slotRepository.findSlot(slotId);

        if (slot.isEmpty()) {
            throw new IllegalArgumentException("Slot not found");
        }

        int currentVersion =
                ((Number) slot.get("VERSION")).intValue();

        /*
         * Optimistic concurrency control:
         *
         * The update succeeds only if the slot still has
         * the same version that we originally read.
         */
        int claimed = slotRepository.claimSlotOptimistically(
                slotId,
                currentVersion
        );

        if (claimed != 1) {
            throw new SlotConflictException(
                    "This slot was booked by another customer"
            );
        }

        try {
            appointmentRepository.create(slotId, customerId);
        } catch (DuplicateKeyException exception) {
            throw new SlotConflictException(
                    "This slot was booked by another customer"
            );
        }
    }

    @Transactional
    public void cancel(
            Long customerId,
            Long appointmentId
    ) {
        int updated = appointmentRepository.cancelOwned(
                appointmentId,
                customerId
        );

        if (updated != 1) {
            throw new IllegalArgumentException(
                    "Appointment not found, already completed, " +
                    "or it does not belong to you"
            );
        }
    }

    public List<AppointmentView> customerAppointments(
            Long customerId
    ) {
        return appointmentRepository.findForCustomer(customerId);
    }

    public List<AppointmentView> providerAppointments(
            Long userId
    ) {
        Long providerId =
                userRepository.findProviderIdByUserId(userId);

        return appointmentRepository.findForProvider(providerId);
    }

    public void createProviderSlot(
            Long userId,
            SlotForm form
    ) {
        if (!form.getEndTime().isAfter(form.getStartTime())) {
            throw new IllegalArgumentException(
                    "End time must be after start time"
            );
        }

        Long providerId =
                userRepository.findProviderIdByUserId(userId);

        slotRepository.create(
                providerId,
                form.getServiceId(),
                form.getStartTime(),
                form.getEndTime()
        );
    }

    public void removeProviderSlot(
            Long userId,
            Long slotId
    ) {
        Long providerId =
                userRepository.findProviderIdByUserId(userId);

        int removed = slotRepository.remove(
                slotId,
                providerId
        );

        if (removed != 1) {
            throw new IllegalArgumentException(
                    "Slot not found or already removed"
            );
        }
    }

    public static class SlotConflictException
            extends RuntimeException {

        public SlotConflictException(String message) {
            super(message);
        }
    }
}
