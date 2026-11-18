package edu.sjsu.cmpe172.careconnect.ai;

import edu.sjsu.cmpe172.careconnect.dto.NotificationRequest;
import edu.sjsu.cmpe172.careconnect.model.UpcomingAppointment;
import edu.sjsu.cmpe172.careconnect.notification.NotificationGateway;
import edu.sjsu.cmpe172.careconnect.repository.AppointmentRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AutonomousReminderService {

    private final AppointmentRepository appointmentRepository;
    private final NotificationGateway notificationGateway;
    private final ObjectProvider<ChatClient> chatClientProvider;
    private final int lookaheadHours;

    public AutonomousReminderService(
            AppointmentRepository appointmentRepository,
            NotificationGateway notificationGateway,
            ObjectProvider<ChatClient> chatClientProvider,
            @Value("${careconnect.ai.reminders.lookahead-hours:48}")
            int lookaheadHours
    ) {
        this.appointmentRepository = appointmentRepository;
        this.notificationGateway = notificationGateway;
        this.chatClientProvider = chatClientProvider;
        this.lookaheadHours = lookaheadHours;
    }

    @Scheduled(
            cron = "${careconnect.ai.reminders.cron:0 0 * * * *}"
    )
    public void scheduledRun() {
        runNow();
    }

    public int runNow() {
        LocalDateTime now = LocalDateTime.now();

        List<UpcomingAppointment> appointments =
                appointmentRepository.findUpcoming(
                        now,
                        now.plusHours(lookaheadHours)
                );

        for (UpcomingAppointment appointment : appointments) {
            String message = generateReminder(appointment);

            NotificationRequest notification =
                    new NotificationRequest(
                            appointment.customerEmail(),
                            "APPOINTMENT_REMINDER",
                            message
                    );

            notificationGateway.send(notification);
        }

        return appointments.size();
    }

    private String generateReminder(
            UpcomingAppointment appointment
    ) {
        ChatClient client = chatClientProvider.getIfAvailable();

        if (client == null) {
            return "Hi "
                    + appointment.customerName()
                    + ", this is a reminder that your "
                    + appointment.serviceName()
                    + " appointment is on "
                    + appointment.startTime()
                    + ".";
        }

        String prompt =
                "Write one concise and friendly appointment reminder "
                        + "for "
                        + appointment.customerName()
                        + ". The appointment is for "
                        + appointment.serviceName()
                        + " and starts at "
                        + appointment.startTime()
                        + ". Do not invent any details.";

        String response = client
                .prompt()
                .user(prompt)
                .call()
                .content();

        return response == null || response.isBlank()
                ? "Reminder: your appointment is on "
                        + appointment.startTime()
                : response;
    }
}