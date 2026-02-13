-- V003: Patients, Programs, Enrollments, and Audit

-- Create programs table
CREATE TABLE programs (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create support_services table
CREATE TABLE support_services (
    id BIGSERIAL PRIMARY KEY,
    program_id BIGINT NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    service_type VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_support_services_program_id ON support_services(program_id);

-- Create patients table
CREATE TABLE patients (
    id BIGSERIAL PRIMARY KEY,
    reference_id VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(20),
    phone VARCHAR(20),
    email VARCHAR(255),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(2),
    zip_code VARCHAR(10),
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_patients_reference_id ON patients(reference_id);
CREATE INDEX idx_patients_last_name ON patients(last_name);
CREATE INDEX idx_patients_created_by ON patients(created_by);

-- Create patient_consents table
CREATE TABLE patient_consents (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    consent_type VARCHAR(50) NOT NULL,
    granted BOOLEAN NOT NULL,
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    granted_by BIGINT NOT NULL REFERENCES users(id),
    notes TEXT
);

CREATE INDEX idx_patient_consents_patient_id ON patient_consents(patient_id);

-- Create enrollments table
CREATE TABLE enrollments (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    program_id BIGINT NOT NULL REFERENCES programs(id),
    prescriber_id BIGINT REFERENCES providers(id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    diagnosis_code VARCHAR(20),
    diagnosis_description TEXT,
    medication_name VARCHAR(255),
    notes TEXT,
    created_by BIGINT NOT NULL REFERENCES users(id),
    submitted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT check_enrollment_status CHECK (status IN ('DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'DENIED', 'WITHDRAWN'))
);

CREATE INDEX idx_enrollments_patient_id ON enrollments(patient_id);
CREATE INDEX idx_enrollments_program_id ON enrollments(program_id);
CREATE INDEX idx_enrollments_status ON enrollments(status);
CREATE INDEX idx_enrollments_created_by ON enrollments(created_by);

-- Create enrollment_status_history table
CREATE TABLE enrollment_status_history (
    id BIGSERIAL PRIMARY KEY,
    enrollment_id BIGINT NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    reason TEXT,
    changed_by BIGINT NOT NULL REFERENCES users(id),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_enrollment_status_history_enrollment_id ON enrollment_status_history(enrollment_id);

-- Create patient_service_enrollments table
CREATE TABLE patient_service_enrollments (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    service_id BIGINT NOT NULL REFERENCES support_services(id),
    enrollment_id BIGINT REFERENCES enrollments(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    enrolled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    enrolled_by BIGINT NOT NULL REFERENCES users(id),
    notes TEXT,

    CONSTRAINT check_service_enrollment_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT unique_patient_service UNIQUE (patient_id, service_id)
);

CREATE INDEX idx_patient_service_enrollments_patient_id ON patient_service_enrollments(patient_id);
CREATE INDEX idx_patient_service_enrollments_service_id ON patient_service_enrollments(service_id);

-- Create audit_events table
CREATE TABLE audit_events (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    user_id BIGINT REFERENCES users(id),
    resource_type VARCHAR(50) NOT NULL,
    resource_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    correlation_id VARCHAR(100),
    ip_address VARCHAR(45),
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_events_user_id ON audit_events(user_id);
CREATE INDEX idx_audit_events_resource_type ON audit_events(resource_type);
CREATE INDEX idx_audit_events_resource_id ON audit_events(resource_id);
CREATE INDEX idx_audit_events_created_at ON audit_events(created_at);
CREATE INDEX idx_audit_events_correlation_id ON audit_events(correlation_id);

-- Seed programs
INSERT INTO programs (name, description, active) VALUES
    ('Sonextra Patient Assistance', 'Comprehensive patient support program providing medication access and financial assistance', true),
    ('CareConnect Services', 'Care coordination and navigation services for patients', true),
    ('PathwayPlus Support', 'Enhanced patient support with personalized care management', true);

-- Seed support services
INSERT INTO support_services (program_id, name, description, service_type) VALUES
    (1, 'Medication Copay Assistance', 'Financial assistance for medication copayments', 'FINANCIAL'),
    (1, 'Free Drug Program', 'Free medication for eligible uninsured patients', 'FINANCIAL'),
    (1, 'Prior Authorization Support', 'Assistance with insurance prior authorization process', 'ADMINISTRATIVE'),
    (1, 'Nursing Education', 'One-on-one nursing education and injection training', 'CLINICAL'),
    (2, 'Care Coordination', 'Personalized care coordination services', 'CARE_MANAGEMENT'),
    (2, 'Appointment Scheduling', 'Help scheduling and coordinating appointments', 'ADMINISTRATIVE'),
    (2, 'Insurance Navigation', 'Guidance through insurance coverage and benefits', 'ADMINISTRATIVE'),
    (3, 'Dedicated Care Manager', 'Assigned care manager for ongoing support', 'CARE_MANAGEMENT'),
    (3, 'Adherence Monitoring', 'Medication adherence monitoring and support', 'CLINICAL'),
    (3, 'Refill Reminders', 'Automated and personalized refill reminders', 'ADMINISTRATIVE');

-- Create sequence for patient reference IDs
CREATE SEQUENCE patient_reference_seq START WITH 10001;
