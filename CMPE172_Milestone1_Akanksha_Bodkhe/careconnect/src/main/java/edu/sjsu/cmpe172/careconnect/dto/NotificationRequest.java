package edu.sjsu.cmpe172.careconnect.dto;

public record NotificationRequest(
        String recipient,
        String type,
        String message
) {}
