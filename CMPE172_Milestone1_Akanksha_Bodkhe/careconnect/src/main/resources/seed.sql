INSERT INTO users (name, email, password_hash, role) VALUES
('Ava Patient', 'ava.patient@example.com', 'demo-hash', 'CUSTOMER'),
('Dr. Maya Chen', 'maya.chen@example.com', 'demo-hash', 'PROVIDER'),
('Jordan Rivera', 'jordan.rivera@example.com', 'demo-hash', 'PROVIDER'),
('CareConnect Admin', 'admin@example.com', 'demo-hash', 'ADMIN');

INSERT INTO providers (user_id, specialization, bio, license_number) VALUES
(2, 'Family Medicine', 'Primary-care provider focused on preventive care.', 'MD-10001'),
(3, 'Physical Therapy', 'Helps patients recover mobility after injury.', 'PT-20002');

INSERT INTO services (service_name, description, duration_minutes, price) VALUES
('General Consultation', 'Routine clinic consultation with a healthcare provider.', 30, 75.00),
('Physical Therapy Evaluation', 'Initial movement and rehabilitation evaluation.', 60, 100.00),
('Preventive Health Visit', 'Wellness and preventive-care appointment.', 45, 90.00);

INSERT INTO availability_slots (provider_id, service_id, start_time, end_time, status) VALUES
(1, 1, '2026-10-05 09:00:00', '2026-10-05 09:30:00', 'AVAILABLE'),
(1, 3, '2026-10-05 10:00:00', '2026-10-05 10:45:00', 'AVAILABLE'),
(2, 2, '2026-10-06 14:00:00', '2026-10-06 15:00:00', 'AVAILABLE'),
(2, 2, '2026-10-07 11:00:00', '2026-10-07 12:00:00', 'AVAILABLE');
