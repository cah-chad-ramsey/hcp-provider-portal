-- Benefits Investigation Table
CREATE TABLE benefits_investigations (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    program_id BIGINT REFERENCES programs(id),
    investigation_type VARCHAR(20) NOT NULL,

    -- Input fields
    payer_name VARCHAR(255),
    payer_plan_id VARCHAR(100),
    member_id VARCHAR(100),
    patient_state VARCHAR(2),
    medication_name VARCHAR(255),

    -- Result fields
    coverage_status VARCHAR(50) NOT NULL,
    coverage_type VARCHAR(50),
    prior_auth_required BOOLEAN,
    deductible_applies BOOLEAN,
    specialty_pharmacy_required BOOLEAN,
    copay_amount DECIMAL(10, 2),
    coinsurance_percentage INTEGER,
    notes TEXT,

    -- Result payload (flexible JSON for additional data)
    result_payload JSONB,

    -- Expiration
    expires_at TIMESTAMP,

    -- Audit fields
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT check_investigation_type CHECK (investigation_type IN ('MEDICAL', 'PHARMACY'))
);

-- Indexes
CREATE INDEX idx_benefits_investigations_patient_id ON benefits_investigations(patient_id);
CREATE INDEX idx_benefits_investigations_created_at ON benefits_investigations(created_at DESC);
CREATE INDEX idx_benefits_investigations_expires_at ON benefits_investigations(expires_at);
