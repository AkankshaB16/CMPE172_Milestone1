package edu.sjsu.cmpe172.careconnect.repository;

import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import edu.sjsu.cmpe172.careconnect.model.UserAccount;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UserAccount> findByEmail(String email) {
        String sql = """
                SELECT user_id, name, email, password_hash, role
                FROM users
                WHERE email = ?
                """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> new UserAccount(
                        resultSet.getLong("user_id"),
                        resultSet.getString("name"),
                        resultSet.getString("email"),
                        resultSet.getString("password_hash"),
                        resultSet.getString("role")
                ),
                email
        ).stream().findFirst();
    }

    public Long findProviderIdByUserId(Long userId) {
        return jdbcTemplate.queryForObject(
                "SELECT provider_id FROM providers WHERE user_id = ?",
                Long.class,
                userId
        );
    }
}
