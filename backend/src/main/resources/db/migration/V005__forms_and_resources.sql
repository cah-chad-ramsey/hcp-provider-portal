-- Form Resources Table
CREATE TABLE form_resources (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    program_id BIGINT REFERENCES programs(id),
    category VARCHAR(100),

    -- File storage
    file_path VARCHAR(500) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,

    -- Version control
    version INTEGER NOT NULL DEFAULT 1,
    parent_id BIGINT REFERENCES form_resources(id),

    -- Compliance
    compliance_approved BOOLEAN NOT NULL DEFAULT false,

    -- Audit fields
    uploaded_by BIGINT NOT NULL REFERENCES users(id),
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Download Audits Table
CREATE TABLE download_audits (
    id BIGSERIAL PRIMARY KEY,
    form_resource_id BIGINT NOT NULL REFERENCES form_resources(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    patient_id BIGINT REFERENCES patients(id),
    downloaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    correlation_id VARCHAR(100),
    ip_address VARCHAR(50)
);

-- Indexes
CREATE INDEX idx_form_resources_program_id ON form_resources(program_id);
CREATE INDEX idx_form_resources_category ON form_resources(category);
CREATE INDEX idx_form_resources_uploaded_at ON form_resources(uploaded_at DESC);
CREATE INDEX idx_form_resources_parent_id ON form_resources(parent_id);
CREATE INDEX idx_download_audits_form_resource_id ON download_audits(form_resource_id);
CREATE INDEX idx_download_audits_user_id ON download_audits(user_id);
CREATE INDEX idx_download_audits_patient_id ON download_audits(patient_id);
CREATE INDEX idx_download_audits_downloaded_at ON download_audits(downloaded_at DESC);
CREATE INDEX idx_download_audits_correlation_id ON download_audits(correlation_id);
