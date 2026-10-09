package edu.sjsu.cmpe172.careconnect.repository;

import edu.sjsu.cmpe172.careconnect.dto.AvailableSlotDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SlotRepository {
    private final JdbcTemplate jdbcTemplate;

    public SlotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int countAvailableSlots() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM availability_slots WHERE status = 'AVAILABLE'",
                Integer.class
        );
    }

    public List<AvailableSlotDto> findAvailableSlots() {
        String sql = """
                SELECT s.slot_id, u.name AS provider_name, p.specialization,
                       sv.service_name, sv.duration_minutes, sv.price,
                       s.start_time, s.end_time
                FROM availability_slots s
                JOIN providers p ON s.provider_id = p.provider_id
                JOIN users u ON p.user_id = u.user_id
                JOIN services sv ON s.service_id = sv.service_id
                WHERE s.status = 'AVAILABLE'
                ORDER BY s.start_time
                """;

        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new AvailableSlotDto(
                resultSet.getLong("slot_id"),
                resultSet.getString("provider_name"),
                resultSet.getString("specialization"),
                resultSet.getString("service_name"),
                resultSet.getInt("duration_minutes"),
                resultSet.getBigDecimal("price"),
                resultSet.getTimestamp("start_time").toLocalDateTime(),
                resultSet.getTimestamp("end_time").toLocalDateTime()
        ));
    }
}
