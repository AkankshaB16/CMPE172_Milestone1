package edu.sjsu.cmpe172.careconnect.model;

public record UserAccount(
        Long userId,
        String name,
        String email,
        String passwordHash,
        String role
) {
    public boolean isCustomer() {
        return "CUSTOMER".equals(role);
    }

    public boolean isProvider() {
        return "PROVIDER".equals(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
    

