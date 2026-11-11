package com.businesslab.workspace.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkspaceTest {

    private static final Instant CREATED_AT =
        Instant.parse("2026-11-11T10:00:00Z");

    private static final Instant UPDATED_AT =
        Instant.parse("2026-11-11T11:00:00Z");

    @Test
    void shouldCreateLocalWorkspace() {
        Workspace workspace = Workspace.create(
            WorkspaceId.generate(),
            " Personal Workspace ",
            WorkspaceStorageMode.LOCAL,
            CREATED_AT
        );

        assertThat(workspace.name())
            .isEqualTo("Personal Workspace");

        assertThat(workspace.storageMode())
            .isEqualTo(WorkspaceStorageMode.LOCAL);

        assertThat(workspace.status())
            .isEqualTo(WorkspaceStatus.ACTIVE);

        assertThat(workspace.createdAt())
            .isEqualTo(CREATED_AT);

        assertThat(workspace.updatedAt())
            .isEqualTo(CREATED_AT);
    }

    @Test
    void shouldCreateCloudWorkspace() {
        Workspace workspace = Workspace.create(
            WorkspaceId.generate(),
            "Cloud Workspace",
            WorkspaceStorageMode.CLOUD,
            CREATED_AT
        );

        assertThat(workspace.storageMode())
            .isEqualTo(WorkspaceStorageMode.CLOUD);
    }

    @Test
    void shouldRenameActiveWorkspace() {
        Workspace workspace = createLocalWorkspace();

        Workspace renamed = workspace.rename(
            "My Businesses",
            UPDATED_AT
        );

        assertThat(renamed.name())
            .isEqualTo("My Businesses");

        assertThat(renamed.updatedAt())
            .isEqualTo(UPDATED_AT);

        assertThat(renamed.id())
            .isEqualTo(workspace.id());
    }

    @Test
    void shouldPromoteLocalWorkspaceToCloud() {
        Workspace workspace = createLocalWorkspace();

        Workspace promoted = workspace.promoteToCloud(UPDATED_AT);

        assertThat(promoted.id())
            .isEqualTo(workspace.id());

        assertThat(promoted.storageMode())
            .isEqualTo(WorkspaceStorageMode.CLOUD);

        assertThat(promoted.status())
            .isEqualTo(WorkspaceStatus.ACTIVE);

        assertThat(workspace.storageMode())
            .isEqualTo(WorkspaceStorageMode.LOCAL);
    }

    @Test
    void shouldNotPromoteAlreadyCloudWorkspace() {
        Workspace workspace = Workspace.create(
            WorkspaceId.generate(),
            "Cloud Workspace",
            WorkspaceStorageMode.CLOUD,
            CREATED_AT
        );

        assertThatThrownBy(
            () -> workspace.promoteToCloud(UPDATED_AT)
        )
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Workspace already uses cloud storage");
    }

    @Test
    void shouldArchiveActiveWorkspace() {
        Workspace workspace = createLocalWorkspace();

        Workspace archived = workspace.archive(UPDATED_AT);

        assertThat(archived.status())
            .isEqualTo(WorkspaceStatus.ARCHIVED);
    }

    @Test
    void shouldNotModifyArchivedWorkspace() {
        Workspace workspace = createLocalWorkspace()
            .archive(UPDATED_AT);

        assertThatThrownBy(
            () -> workspace.rename(
                "New Name",
                UPDATED_AT.plusSeconds(60)
            )
        )
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Archived workspaces cannot be modified");
    }

    @Test
    void shouldRejectBlankWorkspaceName() {
        assertThatThrownBy(
            () -> Workspace.create(
                WorkspaceId.generate(),
                " ",
                WorkspaceStorageMode.LOCAL,
                CREATED_AT
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Workspace name cannot be blank");
    }

    @Test
    void shouldRejectUpdateTimestampBeforeCreation() {
        assertThatThrownBy(
            () -> Workspace.create(
                WorkspaceId.generate(),
                "Workspace",
                WorkspaceStorageMode.LOCAL,
                CREATED_AT
            ).rename(
                "Renamed Workspace",
                CREATED_AT.minusSeconds(1)
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(
                "Update timestamp cannot be before the previous update"
            );
    }

    private Workspace createLocalWorkspace() {
        return Workspace.create(
            WorkspaceId.generate(),
            "Personal Workspace",
            WorkspaceStorageMode.LOCAL,
            CREATED_AT
        );
    }
}
