-- Secure Message Threads
CREATE TABLE secure_message_threads (
    id BIGSERIAL PRIMARY KEY,
    subject VARCHAR(255) NOT NULL,
    program_id BIGINT,
    patient_id BIGINT,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_message_at TIMESTAMP,
    CONSTRAINT fk_thread_program FOREIGN KEY (program_id) REFERENCES programs(id),
    CONSTRAINT fk_thread_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_thread_creator FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX idx_thread_created_by ON secure_message_threads(created_by);
CREATE INDEX idx_thread_patient ON secure_message_threads(patient_id);
CREATE INDEX idx_thread_program ON secure_message_threads(program_id);
CREATE INDEX idx_thread_last_message ON secure_message_threads(last_message_at DESC);

-- Secure Messages
CREATE TABLE secure_messages (
    id BIGSERIAL PRIMARY KEY,
    thread_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    sent_by BIGINT NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    CONSTRAINT fk_message_thread FOREIGN KEY (thread_id) REFERENCES secure_message_threads(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_sender FOREIGN KEY (sent_by) REFERENCES users(id)
);

CREATE INDEX idx_message_thread ON secure_messages(thread_id);
CREATE INDEX idx_message_sent_at ON secure_messages(sent_at DESC);

-- Message Attachments
CREATE TABLE message_attachments (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100),
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attachment_message FOREIGN KEY (message_id) REFERENCES secure_messages(id) ON DELETE CASCADE
);

CREATE INDEX idx_attachment_message ON message_attachments(message_id);

-- Comments
COMMENT ON TABLE secure_message_threads IS 'Message threads for secure communication between staff, agents, and providers';
COMMENT ON TABLE secure_messages IS 'Individual messages within threads';
COMMENT ON TABLE message_attachments IS 'File attachments for messages stored in MinIO';
