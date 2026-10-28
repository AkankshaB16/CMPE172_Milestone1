package edu.sjsu.cmpe172.careconnect.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class BookingMetrics {
    private final AtomicLong successfulBookings = new AtomicLong();
    private final AtomicLong failedBookings = new AtomicLong();
    private final AtomicLong totalBookingLatencyMs = new AtomicLong();

    public void recordSuccess(long latencyMs) {
        successfulBookings.incrementAndGet();
        totalBookingLatencyMs.addAndGet(latencyMs);
    }

    public void recordFailure() {
        failedBookings.incrementAndGet();
    }

    public long successfulBookings() { return successfulBookings.get(); }
    public long failedBookings() { return failedBookings.get(); }
    public long averageBookingLatencyMs() {
        long count = successfulBookings.get();
        return count == 0 ? 0 : totalBookingLatencyMs.get() / count;
    }
}
