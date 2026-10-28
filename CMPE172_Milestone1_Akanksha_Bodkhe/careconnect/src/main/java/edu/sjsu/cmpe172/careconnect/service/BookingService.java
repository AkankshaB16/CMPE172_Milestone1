package edu.sjsu.cmpe172.careconnect.service;

import edu.sjsu.cmpe172.careconnect.dto.AvailableSlotDto;
import edu.sjsu.cmpe172.careconnect.dto.SlotForm;
import edu.sjsu.cmpe172.careconnect.model.AppointmentView;
import edu.sjsu.cmpe172.careconnect.notification.NotificationGateway;
import edu.sjsu.cmpe172.careconnect.repository.AppointmentRepository;
import edu.sjsu.cmpe172.careconnect.repository.SlotRepository;
import edu.sjsu.cmpe172.careconnect.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {
    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final SlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final BookingTransactionService transactionService;
    private final NotificationGateway notificationGateway;
    private final BookingMetrics metrics;

    public BookingService(
            SlotRepository slotRepository,
            AppointmentRepository appointmentRepository,
            UserRepository userRepository,
            BookingTransactionService transactionService,
            NotificationGateway notificationGateway,
            BookingMetrics metrics
    ) {
        this.slotRepository = slotRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.transactionService = transactionService;
        this.notificationGateway = notificationGateway;
        this.metrics = metrics;
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

    public void book(Long customerId, Long slotId) {
        long started = System.currentTimeMillis();
        try {
            var notification = transactionService.book(customerId, slotId);
            // The transaction has committed before the remote call begins.
            try {
                notificationGateway.send(notification);
            } catch (RuntimeException exception) {
                // Fallback: retain the committed booking when the remote service is down.
                log.error("Notification unavailable after booking; fallback accepted: slotId={}",
                        slotId, exception);
            }
            metrics.recordSuccess(System.currentTimeMillis() - started);
            log.info("Booking committed: customerId={}, slotId={}", customerId, slotId);
        } catch (RuntimeException exception) {
            metrics.recordFailure();
            log.warn("Booking failed: customerId={}, slotId={}", customerId, slotId);
            throw exception;
        }
    }

    public void cancel(
            Long customerId,
            Long appointmentId
    ) {
        var notification = transactionService.cancel(customerId, appointmentId);
        // Cancellation is committed before its confirmation is sent.
        try {
            notificationGateway.send(notification);
        } catch (RuntimeException exception) {
            log.error("Notification unavailable after cancellation; fallback accepted: appointmentId={}",
                    appointmentId, exception);
        }
        log.info("Cancellation committed: customerId={}, appointmentId={}",
                customerId, appointmentId);
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
