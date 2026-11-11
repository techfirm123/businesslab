
CREATE TABLE app_user (
                          user_id UUID PRIMARY KEY,
                          email VARCHAR(320) NOT NULL,
                          display_name VARCHAR(100) NOT NULL,
                          status VARCHAR(20) NOT NULL,
                          created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                          updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                          CONSTRAINT uq_app_user_email
                              UNIQUE (email),

                          CONSTRAINT chk_app_user_email_normalized
                              CHECK (email = LOWER(email)),

                          CONSTRAINT chk_app_user_display_name
                              CHECK (LENGTH(TRIM(display_name)) BETWEEN 1 AND 100),

                          CONSTRAINT chk_app_user_status
                              CHECK (status IN ('ACTIVE', 'SUSPENDED')),

                          CONSTRAINT chk_app_user_timestamps
                              CHECK (updated_at >= created_at)
);

CREATE INDEX idx_app_user_status
    ON app_user (status);


CREATE TABLE user_password_credential (
                                          user_id UUID PRIMARY KEY,
                                          password_hash VARCHAR(255) NOT NULL,
                                          created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                          updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                          CONSTRAINT fk_user_password_credential_user
                                              FOREIGN KEY (user_id)
                                                  REFERENCES app_user (user_id)
                                                  ON DELETE CASCADE,

                                          CONSTRAINT chk_user_password_credential_hash
                                              CHECK (LENGTH(password_hash) > 0),

                                          CONSTRAINT chk_user_password_credential_timestamps
                                              CHECK (updated_at >= created_at)
);
