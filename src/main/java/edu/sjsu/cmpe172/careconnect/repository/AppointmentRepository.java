package edu.sjsu.cmpe172.careconnect.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import edu.sjsu.cmpe172.careconnect.model.AppointmentView;

@Repository
public class AppointmentRepository {

    private final JdbcTemplate jdbcTemplate;

    public AppointmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

   

    public int create(Long slotId, Long customerId) {
        int reused = jdbcTemplate.update(
                """
                UPDATE appointments
                SET customer_id = ?,
                    status = 'BOOKED',
                    booked_at = CURRENT_TIMESTAMP
                WHERE slot_id = ?
                  AND status = 'CANCELLED'
                """,
                customerId,
                slotId
        );

        if (reused == 1) {
            return 1;
        }

        return jdbcTemplate.update(
                """
                INSERT INTO appointments
                (slot_id, customer_id, status)
                VALUES (?, ?, 'BOOKED')
                """,
                slotId,
                customerId
        );
    }

    public List<AppointmentView> findForCustomer(Long customerId) {
        String sql = """
                SELECT
                    a.appointment_id,
                    a.slot_id,
                    customer.name AS customer_name,
                    provider_user.name AS provider_name,
                    sv.service_name,
                    s.start_time,
                    s.end_time,
                    CASE
                        WHEN a.status = 'BOOKED'
                             AND s.end_time < CURRENT_TIMESTAMP
                        THEN 'COMPLETED'
                        ELSE a.status
                    END AS appointment_status
                FROM appointments a
                JOIN availability_slots s
                    ON a.slot_id = s.slot_id
                JOIN users customer
                    ON a.customer_id = customer.user_id
                JOIN providers p
                    ON s.provider_id = p.provider_id
                JOIN users provider_user
                    ON p.user_id = provider_user.user_id
                JOIN services sv
                    ON s.service_id = sv.service_id
                WHERE a.customer_id = ?
                ORDER BY s.start_time
                """;

        return jdbcTemplate.query(sql, appointmentRowMapper(), customerId);
    }

    public List<AppointmentView> findForProvider(Long providerId) {
        String sql = """
                SELECT
                    a.appointment_id,
                    a.slot_id,
                    customer.name AS customer_name,
                    provider_user.name AS provider_name,
                    sv.service_name,
                    s.start_time,
                    s.end_time,
                    CASE
                        WHEN a.status = 'BOOKED'
                             AND s.end_time < CURRENT_TIMESTAMP
                        THEN 'COMPLETED'
                        ELSE a.status
                    END AS appointment_status
                FROM appointments a
                JOIN availability_slots s
                    ON a.slot_id = s.slot_id
                JOIN users customer
                    ON a.customer_id = customer.user_id
                JOIN providers p
                    ON s.provider_id = p.provider_id
                JOIN users provider_user
                    ON p.user_id = provider_user.user_id
                JOIN services sv
                    ON s.service_id = sv.service_id
                WHERE s.provider_id = ?
                ORDER BY s.start_time
                """;

        return jdbcTemplate.query(sql, appointmentRowMapper(), providerId);
    }

    private org.springframework.jdbc.core.RowMapper<AppointmentView>
    appointmentRowMapper() {
        return (resultSet, rowNumber) -> new AppointmentView(
                resultSet.getLong("appointment_id"),
                resultSet.getLong("slot_id"),
                resultSet.getString("customer_name"),
                resultSet.getString("provider_name"),
                resultSet.getString("service_name"),
                resultSet.getTimestamp("start_time")
                        .toLocalDateTime(),
                resultSet.getTimestamp("end_time")
                        .toLocalDateTime(),
                resultSet.getString("appointment_status")
        );
    }

    public int cancelOwned(
            Long appointmentId,
            Long customerId
    ) {
        return jdbcTemplate.update(
                """
                UPDATE appointments
                SET status = 'CANCELLED'
                WHERE appointment_id = ?
                  AND customer_id = ?
                  AND status = 'BOOKED'
                  AND EXISTS (
                      SELECT 1
                      FROM availability_slots s
                      WHERE s.slot_id = appointments.slot_id
                        AND s.end_time > CURRENT_TIMESTAMP
                  )
                """,
                appointmentId,
                customerId
        );
    }
}