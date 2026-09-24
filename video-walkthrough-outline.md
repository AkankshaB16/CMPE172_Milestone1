# CareConnect Milestone 1 Code-Walkthrough Video

Minimum length: 5 minutes.

## 0:00–1:00 — Project overview

Explain that CareConnect is a clinic appointment-booking system. Patients are customers, and providers are doctors, nurses, therapists, or other healthcare professionals. Explain that Milestone 1 builds a database-backed skeleton, not all twelve features.

## 1:00–2:00 — Database design

Open `src/main/resources/schema.sql`. Explain the five tables, primary keys, foreign keys, required fields, and the `UNIQUE (slot_id)` constraint. Open `seed.sql` and explain how sample patients, providers, services, and availability slots are inserted.

## 2:00–3:00 — Layered code

Open `CareConnectController`. Explain that the Controller receives HTTP requests. Open `CareConnectService` and explain that the Service coordinates application behavior. Open `SlotRepository` and explain that it executes handwritten SQL using `JdbcTemplate`. Open the DTO records and explain that they carry response data without exposing database implementation details.

## 3:00–4:00 — Request flow

Trace `GET /slots`: the request reaches the Controller, the Controller calls the Service, the Service calls the Repository, the Repository executes a SQL join, and the result is mapped into `AvailableSlotDto` objects and returned as JSON.

## 4:00–5:00 — Run and demonstrate

Run `mvn spring-boot:run`, open `/`, and open `/slots`. Explain that Spring Boot loads `schema.sql` and `seed.sql` at startup. Show the returned database-backed JSON. Mention that booking, cancellation, notifications, monitoring, and AI are future milestones.

## Optional additional explanation

Explain Page Controller versus Front Controller. Server-rendered page handlers resemble Page Controllers, while Spring MVC’s `DispatcherServlet` acts as a Front Controller that dispatches incoming requests to the correct controller method.
