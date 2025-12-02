-- ===============================
-- MFA TABLE FOR STORING USER MFA SETTINGS
-- ===============================
CREATE TABLE user_mfa_settings (
    mfa_id SERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    mfa_enabled BOOLEAN DEFAULT FALSE,
    mfa_secret VARCHAR(255), -- TOTP secret key
    backup_codes TEXT[], -- Array of backup codes
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id)
);

-- Trigger auto-update timestamp
CREATE TRIGGER trigger_update_user_mfa_settings
BEFORE UPDATE ON user_mfa_settings
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

-- Index for faster lookups
CREATE INDEX idx_user_mfa_settings_user_id ON user_mfa_settings(user_id);

