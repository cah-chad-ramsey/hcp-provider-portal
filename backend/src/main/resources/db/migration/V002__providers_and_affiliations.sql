-- V002: Providers and Provider Affiliations

-- Create providers table
CREATE TABLE providers (
    id BIGSERIAL PRIMARY KEY,
    npi VARCHAR(10) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    specialty VARCHAR(100),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(2),
    zip_code VARCHAR(10),
    phone VARCHAR(20),
    fax VARCHAR(20),
    email VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_providers_npi ON providers(npi);
CREATE INDEX idx_providers_name ON providers(name);
CREATE INDEX idx_providers_active ON providers(active);

-- Create provider_affiliations table
CREATE TABLE provider_affiliations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider_id BIGINT NOT NULL REFERENCES providers(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP,
    verified_by BIGINT REFERENCES users(id),
    verification_reason TEXT,

    CONSTRAINT unique_user_provider UNIQUE (user_id, provider_id),
    CONSTRAINT check_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE INDEX idx_provider_affiliations_user_id ON provider_affiliations(user_id);
CREATE INDEX idx_provider_affiliations_provider_id ON provider_affiliations(provider_id);
CREATE INDEX idx_provider_affiliations_status ON provider_affiliations(status);

-- Seed sample providers
INSERT INTO providers (npi, name, specialty, address_line1, city, state, zip_code, phone, email) VALUES
    ('1234567890', 'Dr. Sarah Johnson', 'Oncology', '123 Medical Center Dr', 'Boston', 'MA', '02101', '617-555-0100', 'sjohnson@example.com'),
    ('2345678901', 'Dr. Michael Chen', 'Rheumatology', '456 Healthcare Plaza', 'New York', 'NY', '10001', '212-555-0200', 'mchen@example.com'),
    ('3456789012', 'Dr. Emily Rodriguez', 'Dermatology', '789 Wellness Blvd', 'Los Angeles', 'CA', '90001', '213-555-0300', 'erodriguez@example.com'),
    ('4567890123', 'Dr. James Wilson', 'Oncology', '321 Care Center Way', 'Chicago', 'IL', '60601', '312-555-0400', 'jwilson@example.com'),
    ('5678901234', 'Dr. Lisa Anderson', 'Rheumatology', '654 Health Services Ave', 'Houston', 'TX', '77001', '713-555-0500', 'landerson@example.com'),
    ('6789012345', 'Dr. Robert Martinez', 'Dermatology', '987 Medical Arts Dr', 'Phoenix', 'AZ', '85001', '602-555-0600', 'rmartinez@example.com'),
    ('7890123456', 'Dr. Jennifer Taylor', 'Oncology', '147 Clinical Center Ln', 'Philadelphia', 'PA', '19101', '215-555-0700', 'jtaylor@example.com'),
    ('8901234567', 'Dr. David Thompson', 'Rheumatology', '258 Provider Plaza', 'San Antonio', 'TX', '78201', '210-555-0800', 'dthompson@example.com'),
    ('9012345678', 'Dr. Maria Garcia', 'Dermatology', '369 Specialty Center St', 'San Diego', 'CA', '92101', '619-555-0900', 'mgarcia@example.com'),
    ('0123456789', 'Dr. Christopher Lee', 'Oncology', '741 Treatment Center Dr', 'Dallas', 'TX', '75201', '214-555-1000', 'clee@example.com');

-- Create a sample affiliation for the staff user (for testing)
-- User ID 2 is staff@sonexus.com
INSERT INTO provider_affiliations (user_id, provider_id, status, requested_at)
VALUES (2, 1, 'PENDING', CURRENT_TIMESTAMP);
