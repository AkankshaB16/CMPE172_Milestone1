package edu.sjsu.cmpe172.careconnect.repository;

import edu.sjsu.cmpe172.careconnect.dto.AvailableSlotDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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
        String sql = """
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
                AND (? IS NULL OR s.provider_id = ?)
                AND (? IS NULL OR s.service_id = ?)
                AND (
                    ? IS NULL
                    OR CAST(s.start_time AS DATE) = CAST(? AS DATE)
                )
                ORDER BY s.start_time
                LIMIT ? OFFSET ?
                """;

        return jdbcTemplate.query(
                sql,
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
                providerId,
                providerId,
                serviceId,
                serviceId,
                date,
                date,
                size,
                page * size
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