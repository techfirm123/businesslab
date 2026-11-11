
CREATE TABLE workspace_membership (
                                      membership_id UUID PRIMARY KEY,
                                      workspace_id UUID NOT NULL,
                                      user_id UUID NOT NULL,
                                      role VARCHAR(20) NOT NULL,
                                      status VARCHAR(20) NOT NULL,
                                      created_at TIMESTAMPTZ NOT NULL,
                                      updated_at TIMESTAMPTZ NOT NULL,

                                      CONSTRAINT uq_workspace_membership_user
                                          UNIQUE (workspace_id, user_id),

                                      CONSTRAINT fk_workspace_membership_workspace
                                          FOREIGN KEY (workspace_id)
                                              REFERENCES workspace (workspace_id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT fk_workspace_membership_user
                                          FOREIGN KEY (user_id)
                                              REFERENCES app_user (user_id)
                                              ON DELETE RESTRICT,

                                      CONSTRAINT chk_workspace_membership_role
                                          CHECK (
                                              role IN (
                                                       'OWNER',
                                                       'ADMIN',
                                                       'MEMBER',
                                                       'VIEWER'
                                                  )
                                              ),

                                      CONSTRAINT chk_workspace_membership_status
                                          CHECK (
                                              status IN (
                                                         'ACTIVE',
                                                         'SUSPENDED',
                                                         'REMOVED'
                                                  )
                                              ),

                                      CONSTRAINT chk_workspace_membership_timestamps
                                          CHECK (updated_at >= created_at)
);

-- A workspace can have at most one OWNER membership.
CREATE UNIQUE INDEX uq_workspace_membership_single_owner
    ON workspace_membership (workspace_id)
    WHERE role = 'OWNER';

CREATE INDEX idx_workspace_membership_user_status
    ON workspace_membership (
                             user_id,
                             status,
                             created_at DESC
        );

CREATE INDEX idx_workspace_membership_workspace_status
    ON workspace_membership (
                             workspace_id,
                             status,
                             role
        );
