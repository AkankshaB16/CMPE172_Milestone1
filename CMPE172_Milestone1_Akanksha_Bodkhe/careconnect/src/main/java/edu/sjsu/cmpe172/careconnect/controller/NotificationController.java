package edu.sjsu.cmpe172.careconnect.controller;

import edu.sjsu.cmpe172.careconnect.dto.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody NotificationRequest request) {
        log.info("Mock notification accepted: type={}, recipient={}, message={}",
                request.type(), request.recipient(), request.message());
        return ResponseEntity.ok().build();
    }
}
