package edu.sjsu.cmpe172.careconnect.notification;

import edu.sjsu.cmpe172.careconnect.dto.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Component
public class HttpNotificationGateway implements NotificationGateway {
    private static final Logger log = LoggerFactory.getLogger(HttpNotificationGateway.class);

    private final RestTemplate restTemplate;
    private final String notificationUrl;
    private final int maxAttempts;

    public HttpNotificationGateway(
            RestTemplateBuilder builder,
            @Value("${notification.service.url:http://localhost:8080/notifications}") String notificationUrl,
            @Value("${notification.client.max-attempts:3}") int maxAttempts,
            @Value("${notification.client.timeout-ms:750}") long timeoutMs
    ) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofMillis(timeoutMs))
                .setReadTimeout(Duration.ofMillis(timeoutMs))
                .build();
        this.notificationUrl = notificationUrl;
        this.maxAttempts = Math.max(1, maxAttempts);
    }

    @Override
    public void send(NotificationRequest request) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                restTemplate.postForEntity(
                        notificationUrl,
                        request,
                        Void.class
                );
                log.info("Notification delivered: type={}, recipient={}, attempt={}",
                        request.type(), request.recipient(), attempt);
                return;
            } catch (RestClientException exception) {
                if (attempt < maxAttempts) {
                    log.warn("Notification attempt {} failed for recipient={}; retrying",
                            attempt, request.recipient());
                } else {
                    log.error("Notification delivery failed after {} attempts for recipient={}; booking remains committed",
                            maxAttempts, request.recipient(), exception);
                }
            }
        }
    }
}
