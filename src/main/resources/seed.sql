INSERT INTO users
(name, email, password_hash, role)
VALUES
(
    'Ava Patient',
    'ava.patient@example.com',
    '$2a$10$7wXbGDdNz7eciZo2gTCT6O8i1hhXO8wTTXLouIl5YPaRoF5hBERby',
    'CUSTOMER'
),
(
    'Noah Customer',
    'noah.customer@example.com',
    '$2a$10$7wXbGDdNz7eciZo2gTCT6O8i1hhXO8wTTXLouIl5YPaRoF5hBERby',
    'CUSTOMER'
),
(
    'Dr. Maya Chen',
    'maya.chen@example.com',
    '$2a$10$7wXbGDdNz7eciZo2gTCT6O8i1hhXO8wTTXLouIl5YPaRoF5hBERby',
    'PROVIDER'
),
(
    'Jordan Rivera',
    'jordan.rivera@example.com',
    '$2a$10$7wXbGDdNz7eciZo2gTCT6O8i1hhXO8wTTXLouIl5YPaRoF5hBERby',
    'PROVIDER'
),
(
    'CareConnect Admin',
    'admin@example.com',
    '$2a$10$7wXbGDdNz7eciZo2gTCT6O8i1hhXO8wTTXLouIl5YPaRoF5hBERby',
    'ADMIN'
);

INSERT INTO providers
(user_id, specialization, bio, license_number)
VALUES
(
    3,
    'Family Medicine',
    'Primary-care provider focused on preventive care.',
    'MD-10001'
),
(
    4,
    'Physical Therapy',
    'Helps patients recover mobility after injury.',
    'PT-20002'
);

INSERT INTO services
(service_name, description, duration_minutes, price)
VALUES
(
    'General Consultation',
    'Routine clinic consultation with a healthcare provider.',
    30,
    75.00
),
(
    'Physical Therapy Evaluation',
    'Initial movement and rehabilitation evaluation.',
    60,
    100.00
),
(
    'Preventive Health Visit',
    'Wellness and preventive-care appointment.',
    45,
    90.00
);

INSERT INTO availability_slots
(provider_id, service_id, start_time, end_time, status)
VALUES
(
    1,
    1,
    '2026-10-10 09:00:00',
    '2026-10-10 09:30:00',
    'AVAILABLE'
),
(
    1,
    3,
    '2026-10-10 10:00:00',
    '2026-10-10 10:45:00',
    'AVAILABLE'
),
(
    2,
    2,
    '2026-10-11 14:00:00',
    '2026-10-11 15:00:00',
    'AVAILABLE'
),
(
    2,
    2,
    '2026-10-12 11:00:00',
    '2026-10-12 12:00:00',
    'AVAILABLE'
);