package edu.sjsu.cmpe172.careconnect.repository;

import edu.sjsu.cmpe172.careconnect.dto.AvailableSlotDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class SlotRepository {

    private final JdbcTemplate jdbcTemplate;

    public SlotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int countAvailableSlots() {
        String sql = """
                SELECT COUNT(*)
                FROM availability_slots s
                WHERE s.status = 'AVAILABLE'
                AND NOT EXISTS (
                    SELECT 1
                    FROM appointments a
                    WHERE a.slot_id = s.slot_id
                    AND a.status = 'BOOKED'
                )
                """;

        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public List<AvailableSlotDto> findAvailableSlots() {
        return findAvailableSlots(null, null, null, 0, 100);
    }

    public List<AvailableSlotDto> findAvailableSlots(
            Long providerId,
            Long serviceId,
            String date,
            int page,
            int size
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    s.slot_id,
                    u.name AS provider_name,
                    p.specialization,
                    sv.service_name,
                    sv.duration_minutes,
                    sv.price,
                    s.start_time,
                    s.end_time
                FROM availability_slots s
                JOIN providers p
                    ON s.provider_id = p.provider_id
                JOIN users u
                    ON p.user_id = u.user_id
                JOIN services sv
                    ON s.service_id = sv.service_id
                WHERE s.status = 'AVAILABLE'
                AND NOT EXISTS (
                    SELECT 1
                    FROM appointments a
                    WHERE a.slot_id = s.slot_id
                    AND a.status = 'BOOKED'
                )
                """);

        List<Object> parameters = new ArrayList<>();

        if (providerId != null) {
            sql.append(" AND s.provider_id = ?");
            parameters.add(providerId);
        }

        if (serviceId != null) {
            sql.append(" AND s.service_id = ?");
            parameters.add(serviceId);
        }

        if (date != null && !date.isBlank()) {
            LocalDate selectedDate = LocalDate.parse(date);

            Timestamp startOfDay =
                    Timestamp.valueOf(selectedDate.atStartOfDay());

            Timestamp startOfNextDay =
                    Timestamp.valueOf(selectedDate.plusDays(1).atStartOfDay());

            sql.append("""
                    AND s.start_time >= ?
                    AND s.start_time < ?
                    """);

            parameters.add(startOfDay);
            parameters.add(startOfNextDay);
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        int offset = safePage * safeSize;

        sql.append("""
                ORDER BY s.start_time
                LIMIT ? OFFSET ?
                """);

        parameters.add(safeSize);
        parameters.add(offset);

        return jdbcTemplate.query(
                sql.toString(),
                (resultSet, rowNumber) -> new AvailableSlotDto(
                        resultSet.getLong("slot_id"),
                        resultSet.getString("provider_name"),
                        resultSet.getString("specialization"),
                        resultSet.getString("service_name"),
                        resultSet.getInt("duration_minutes"),
                        resultSet.getBigDecimal("price"),
                        resultSet.getTimestamp("start_time")
                                .toLocalDateTime(),
                        resultSet.getTimestamp("end_time")
                                .toLocalDateTime()
                ),
                parameters.toArray()
        );
    }

    public Map<String, Object> findSlot(Long slotId) {
        return jdbcTemplate.queryForMap(
                """
                SELECT slot_id, status, version, provider_id
                FROM availability_slots
                WHERE slot_id = ?
                """,
                slotId
        );
    }

    public int claimSlotOptimistically(Long slotId, int version) {
        return jdbcTemplate.update(
                """
                UPDATE availability_slots
                SET version = version + 1
                WHERE slot_id = ?
                AND status = 'AVAILABLE'
                AND version = ?
                """,
                slotId,
                version
        );
    }

    public int create(
            Long providerId,
            Long serviceId,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        return jdbcTemplate.update(
                """
                INSERT INTO availability_slots
                (provider_id, service_id, start_time, end_time, status)
                VALUES (?, ?, ?, ?, 'AVAILABLE')
                """,
                providerId,
                serviceId,
                Timestamp.valueOf(startTime),
                Timestamp.valueOf(endTime)
        );
    }

    public int remove(Long slotId, Long providerId) {
        return jdbcTemplate.update(
                """
                UPDATE availability_slots
                SET status = 'REMOVED'
                WHERE slot_id = ?
                AND provider_id = ?
                AND status = 'AVAILABLE'
                """,
                slotId,
                providerId
        );
    }
}