package edu.sjsu.cmpe172.careconnect.notification;

import edu.sjsu.cmpe172.careconnect.dto.NotificationRequest;

public interface NotificationGateway {
    void send(NotificationRequest request);
}
