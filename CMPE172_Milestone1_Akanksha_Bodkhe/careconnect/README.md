# CareConnect

CareConnect is a clinic appointment-booking application developed for CMPE172 Milestone 2.

Customers can log in, view healthcare providers and available appointment slots, book appointments, view their appointments, and cancel their own appointments. Providers can log in, view their availability, and view appointments booked with them.

## Technology Stack

- Java 17
- Spring Boot 3.3.5
- Maven
- Spring MVC
- Thymeleaf
- JDBC with `JdbcTemplate`
- H2 in-memory database
- BCrypt password hashing
- SQL schema and seed scripts

This project uses direct SQL through JDBC. It does not use JPA or any ORM framework.

## Project Structure

```text
src/main/java/edu/sjsu/cmpe172/careconnect/
├── controller/
├── service/
├── repository/
├── dto/
├── model/
└── CareConnectApplication.java

src/main/resources/
├── templates/
├── static/
├── schema.sql
├── seed.sql
milestone2
└── application.properties
main
