
CREATE TABLE workspace (
                           workspace_id UUID PRIMARY KEY,
                           name VARCHAR(100) NOT NULL,
                           storage_mode VARCHAR(20) NOT NULL,
                           status VARCHAR(20) NOT NULL,
                           created_at TIMESTAMPTZ NOT NULL,
                           updated_at TIMESTAMPTZ NOT NULL,

                           CONSTRAINT chk_workspace_storage_mode
                               CHECK (storage_mode = 'CLOUD'),

                           CONSTRAINT chk_workspace_status
                               CHECK (status IN ('ACTIVE', 'ARCHIVED')),

                           CONSTRAINT chk_workspace_timestamps
                               CHECK (updated_at >= created_at)
);

CREATE INDEX idx_workspace_created
    ON workspace (created_at DESC);


CREATE TABLE business_template (
                                   template_id VARCHAR(100) PRIMARY KEY,
                                   source_type VARCHAR(20) NOT NULL,
                                   visibility VARCHAR(20) NOT NULL,
                                   owner_workspace_id UUID,
                                   created_at TIMESTAMPTZ NOT NULL,
                                   updated_at TIMESTAMPTZ NOT NULL,

                                   CONSTRAINT uq_business_template_source
                                       UNIQUE (template_id, source_type),

                                   CONSTRAINT chk_business_template_source
                                       CHECK (source_type IN ('CODE_BACKED', 'DECLARATIVE')),

                                   CONSTRAINT chk_business_template_visibility
                                       CHECK (visibility IN ('PLATFORM', 'PRIVATE', 'SHARED', 'PUBLIC')),

                                   CONSTRAINT fk_business_template_owner_workspace
                                       FOREIGN KEY (owner_workspace_id)
                                           REFERENCES workspace (workspace_id),

                                   CONSTRAINT chk_business_template_scope
                                       CHECK (
                                           (visibility = 'PLATFORM' AND owner_workspace_id IS NULL)
                                               OR
                                           (visibility <> 'PLATFORM' AND owner_workspace_id IS NOT NULL)
                                           ),

                                   CONSTRAINT chk_business_template_timestamps
                                       CHECK (updated_at >= created_at)
);

CREATE INDEX idx_business_template_visibility
    ON business_template (visibility, template_id);


CREATE TABLE business_template_version (
                                           template_id VARCHAR(100) NOT NULL,
                                           version VARCHAR(50) NOT NULL,
                                           source_type VARCHAR(20) NOT NULL,
                                           name VARCHAR(120) NOT NULL,
                                           description VARCHAR(1000) NOT NULL,
                                           category VARCHAR(40) NOT NULL,
                                           lifecycle_status VARCHAR(20) NOT NULL,

                                           definition_schema_version INTEGER,
                                           definition JSONB,

                                           capabilities JSONB NOT NULL DEFAULT '[]'::jsonb,
                                           configuration_schema JSONB NOT NULL DEFAULT '[]'::jsonb,
                                           provenance JSONB NOT NULL DEFAULT '{}'::jsonb,

                                           created_by UUID,
                                           approved_by UUID,
                                           approved_at TIMESTAMPTZ,
                                           published_at TIMESTAMPTZ,
                                           created_at TIMESTAMPTZ NOT NULL,

                                           CONSTRAINT pk_business_template_version
                                               PRIMARY KEY (template_id, version),

                                           CONSTRAINT fk_template_version_template
                                               FOREIGN KEY (template_id, source_type)
                                                   REFERENCES business_template (template_id, source_type),

                                           CONSTRAINT chk_template_version_source
                                               CHECK (source_type IN ('CODE_BACKED', 'DECLARATIVE')),

                                           CONSTRAINT chk_template_version_status
                                               CHECK (
                                                   lifecycle_status IN (
                                                                        'DRAFT',
                                                                        'VALIDATING',
                                                                        'APPROVED',
                                                                        'PUBLISHED',
                                                                        'DEPRECATED',
                                                                        'REJECTED'
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
                                                           AND jsonb_typeof(definition) = 'object'
                                                           AND definition_schema_version IS NOT NULL
                                                           AND definition_schema_version > 0
                                                       )
                                                   ),

                                           CONSTRAINT chk_template_version_json_shapes
                                               CHECK (
                                                   jsonb_typeof(capabilities) = 'array'
                                                       AND jsonb_typeof(configuration_schema) = 'array'
                                                       AND jsonb_typeof(provenance) = 'object'
                                                   ),

                                           CONSTRAINT chk_template_version_approval
                                               CHECK (
                                                   source_type = 'CODE_BACKED'
                                                       OR lifecycle_status NOT IN (
                                                                                   'APPROVED',
                                                                                   'PUBLISHED',
                                                                                   'DEPRECATED'
                                                       )
                                                       OR (
                                                       approved_at IS NOT NULL
                                                           AND approved_by IS NOT NULL
                                                       )
                                                   ),

                                           CONSTRAINT chk_template_version_publication
                                               CHECK (
                                                   lifecycle_status NOT IN ('PUBLISHED', 'DEPRECATED')
                                                       OR published_at IS NOT NULL
                                                   )
);

CREATE INDEX idx_template_version_catalog
    ON business_template_version (
                                  lifecycle_status,
                                  category,
                                  name
        );


CREATE TABLE business (
                          business_id UUID PRIMARY KEY,
                          workspace_id UUID NOT NULL,
                          template_id VARCHAR(100) NOT NULL,
                          template_version VARCHAR(50) NOT NULL,
                          name VARCHAR(120) NOT NULL,
                          description VARCHAR(1000) NOT NULL DEFAULT '',
                          status VARCHAR(20) NOT NULL,
                          created_at TIMESTAMPTZ NOT NULL,
                          updated_at TIMESTAMPTZ NOT NULL,

                          CONSTRAINT fk_business_workspace
                              FOREIGN KEY (workspace_id)
                                  REFERENCES workspace (workspace_id),

                          CONSTRAINT fk_business_template_version
                              FOREIGN KEY (template_id, template_version)
                                  REFERENCES business_template_version (template_id, version),

                          CONSTRAINT chk_business_status
                              CHECK (
                                  status IN ('DRAFT', 'ACTIVE', 'PAUSED', 'CLOSED', 'ARCHIVED')
                                  ),

                          CONSTRAINT chk_business_timestamps
                              CHECK (updated_at >= created_at)
);

CREATE INDEX idx_business_workspace_created
    ON business (workspace_id, created_at DESC);

CREATE INDEX idx_business_template_reference
    ON business (template_id, template_version);


CREATE TABLE business_configuration (
                                        business_id UUID PRIMARY KEY,
                                        configuration JSONB NOT NULL,
                                        schema_version INTEGER NOT NULL DEFAULT 1,
                                        updated_at TIMESTAMPTZ NOT NULL,

                                        CONSTRAINT fk_business_configuration_business
                                            FOREIGN KEY (business_id)
                                                REFERENCES business (business_id)
                                                ON DELETE CASCADE,

                                        CONSTRAINT chk_business_configuration_schema_version
                                            CHECK (schema_version > 0),

                                        CONSTRAINT chk_business_configuration_object
                                            CHECK (jsonb_typeof(configuration) = 'object')
);


-- Register the existing platform-owned Coffee Shop implementation.
-- Its Java implementation remains the runtime authority.
INSERT INTO business_template (
    template_id,
    source_type,
    visibility,
    owner_workspace_id,
    created_at,
    updated_at
) VALUES (
             'coffee-shop',
             'CODE_BACKED',
             'PLATFORM',
             NULL,
             CURRENT_TIMESTAMP,
             CURRENT_TIMESTAMP
         );


-- Persist the exact descriptor/version needed by business foreign keys.
INSERT INTO business_template_version (
    template_id,
    version,
    source_type,
    name,
    description,
    category,
    lifecycle_status,
    definition_schema_version,
    definition,
    capabilities,
    configuration_schema,
    provenance,
    published_at,
    created_at
) VALUES (
             'coffee-shop',
             '1.0.0',
             'CODE_BACKED',
             'Coffee Shop',
             'Start and configure a virtual coffee shop. This template introduces the initial business setup experience for BusinessLab.',
             'FOOD_AND_BEVERAGE',
             'PUBLISHED',
             NULL,
             NULL,
             '["BUSINESS_SETUP"]'::jsonb,
             '[
               {
                 "key": "initialCapital",
                 "label": "Initial Capital",
                 "description": "The starting capital available to establish the business.",
                 "type": "DECIMAL",
                 "required": true,
                 "example": "100000",
                 "minimum": 0.01
               },
               {
                 "key": "currency",
                 "label": "Currency",
                 "description": "The currency used for the business financial configuration.",
                 "type": "STRING",
                 "required": true,
                 "example": "INR",
                 "maxLength": 3,
                 "pattern": "^[A-Z]{3}$"
               }
             ]'::jsonb,
             '{
               "origin": "platform-code",
               "implementation": "com.businesslab.industries.coffeeshop.template.CoffeeShopBusinessTemplate"
             }'::jsonb,
             CURRENT_TIMESTAMP,
             CURRENT_TIMESTAMP
         );
