# CareConnect

CareConnect is a clinic appointment-booking system for CMPE172 Milestone 1. Patients can eventually browse and book appointments with healthcare providers. Milestone 1 implements the layered database-reading skeleton.

## Technology

- Java 17
- Spring Boot 3.3.5
- Maven
- H2 relational database
- Spring JDBC / `JdbcTemplate`
- REST endpoints
- No ORM, JPA, Hibernate, or Spring Data JPA

## Run the application

From the `careconnect` directory:

```bash
mvn spring-boot:run
```

The application starts at `http://localhost:8080`.

Spring runs `schema.sql` and `seed.sql` on startup. The in-memory H2 database contains sample patients, healthcare providers, services, and availability slots.

## Milestone 1 endpoints

### Home

```text
GET http://localhost:8080/
```

### Available slots

```text
GET http://localhost:8080/slots
```

## Architecture

```text
Client → Controller → Service → Repository/JDBC → H2 SQL Database
```

The repository uses manually written SQL through `JdbcTemplate`. The database contains a `UNIQUE(slot_id)` constraint in `appointments`, which prevents double-booking.

## Project structure

```text
src/main/java/edu/sjsu/cmpe172/careconnect/
├── controller/
├── service/
├── repository/
└── dto/
```
