
CREATE TABLE workspace (
                           workspace_id UUID PRIMARY KEY,
                           name VARCHAR(100) NOT NULL,
                           storage_mode VARCHAR(20) NOT NULL,
                           status VARCHAR(20) NOT NULL,
                           created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                           updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                           CONSTRAINT chk_workspace_storage_mode CHECK (storage_mode = 'CLOUD'),
                           CONSTRAINT chk_workspace_status CHECK (status IN ('ACTIVE', 'ARCHIVED')),
                           CONSTRAINT chk_workspace_timestamps CHECK (updated_at >= created_at)
);

CREATE TABLE business_template (
                                   template_id VARCHAR(100) PRIMARY KEY,
                                   source_type VARCHAR(20) NOT NULL,
                                   visibility VARCHAR(20) NOT NULL,
                                   owner_workspace_id UUID,
                                   created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                   updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                   CONSTRAINT uq_business_template_source UNIQUE (template_id, source_type),
                                   CONSTRAINT chk_business_template_source
                                       CHECK (source_type IN ('CODE_BACKED', 'DECLARATIVE')),
                                   CONSTRAINT chk_business_template_visibility
                                       CHECK (visibility IN ('PLATFORM', 'PRIVATE', 'SHARED', 'PUBLIC')),
                                   CONSTRAINT fk_business_template_owner_workspace
                                       FOREIGN KEY (owner_workspace_id) REFERENCES workspace (workspace_id),
                                   CONSTRAINT chk_business_template_scope
                                       CHECK (
                                           (visibility = 'PLATFORM' AND owner_workspace_id IS NULL)
                                               OR
                                           (visibility <> 'PLATFORM' AND owner_workspace_id IS NOT NULL)
                                           ),
                                   CONSTRAINT chk_business_template_timestamps CHECK (updated_at >= created_at)
);

CREATE TABLE business_template_version (
                                           template_id VARCHAR(100) NOT NULL,
                                           version VARCHAR(50) NOT NULL,
                                           source_type VARCHAR(20) NOT NULL,
                                           name VARCHAR(120) NOT NULL,
                                           description VARCHAR(1000) NOT NULL,
                                           category VARCHAR(40) NOT NULL,
                                           lifecycle_status VARCHAR(20) NOT NULL,
                                           definition_schema_version INTEGER,
                                           definition JSON,
                                           capabilities JSON NOT NULL,
                                           configuration_schema JSON NOT NULL,
                                           provenance JSON NOT NULL,
                                           created_by UUID,
                                           approved_by UUID,
                                           approved_at TIMESTAMP WITH TIME ZONE,
                                           published_at TIMESTAMP WITH TIME ZONE,
                                           created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                           CONSTRAINT pk_business_template_version PRIMARY KEY (template_id, version),
                                           CONSTRAINT fk_template_version_template
                                               FOREIGN KEY (template_id, source_type)
                                                   REFERENCES business_template (template_id, source_type),
                                           CONSTRAINT chk_template_version_source
                                               CHECK (source_type IN ('CODE_BACKED', 'DECLARATIVE')),
                                           CONSTRAINT chk_template_version_status
                                               CHECK (
                                                   lifecycle_status IN (
                                                                        'DRAFT', 'VALIDATING', 'APPROVED',
                                                                        'PUBLISHED', 'DEPRECATED', 'REJECTED'
                                                       )
                                                   ),
                                           CONSTRAINT chk_template_version_definition
                                               CHECK (
                                                   (
                                                       source_type = 'CODE_BACKED'
                                                           AND definition IS NULL
                                                           AND definition_schema_version IS NULL
                                                       )
                                                       OR
                                                   (
                                                       source_type = 'DECLARATIVE'
                                                           AND definition IS NOT NULL
                                                           AND definition_schema_version > 0
                                                       )
                                                   ),
                                           CONSTRAINT chk_template_version_approval
                                               CHECK (
                                                   source_type = 'CODE_BACKED'
                                                       OR lifecycle_status NOT IN ('APPROVED', 'PUBLISHED', 'DEPRECATED')
                                                       OR (approved_at IS NOT NULL AND approved_by IS NOT NULL)
                                                   ),
                                           CONSTRAINT chk_template_version_publication
                                               CHECK (
                                                   lifecycle_status NOT IN ('PUBLISHED', 'DEPRECATED')
                                                       OR published_at IS NOT NULL
                                                   )
);

CREATE TABLE business (
                          business_id UUID PRIMARY KEY,
                          workspace_id UUID NOT NULL,
                          template_id VARCHAR(100) NOT NULL,
                          template_version VARCHAR(50) NOT NULL,
                          name VARCHAR(120) NOT NULL,
                          description VARCHAR(1000) NOT NULL DEFAULT '',
                          status VARCHAR(20) NOT NULL,
                          created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                          updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                          CONSTRAINT fk_business_workspace
                              FOREIGN KEY (workspace_id) REFERENCES workspace (workspace_id),
                          CONSTRAINT fk_business_template_version
                              FOREIGN KEY (template_id, template_version)
                                  REFERENCES business_template_version (template_id, version),
                          CONSTRAINT chk_business_status
                              CHECK (status IN ('DRAFT', 'ACTIVE', 'PAUSED', 'CLOSED', 'ARCHIVED')),
                          CONSTRAINT chk_business_timestamps CHECK (updated_at >= created_at)
);

CREATE TABLE business_configuration (
                                        business_id UUID PRIMARY KEY,
                                        configuration JSON NOT NULL,
                                        schema_version INTEGER NOT NULL DEFAULT 1,
                                        updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                        CONSTRAINT fk_business_configuration_business
                                            FOREIGN KEY (business_id)
                                                REFERENCES business (business_id)
                                                ON DELETE CASCADE,
                                        CONSTRAINT chk_business_configuration_schema_version CHECK (schema_version > 0)
);
