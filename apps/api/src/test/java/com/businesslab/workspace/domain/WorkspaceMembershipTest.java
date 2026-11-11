
package com.businesslab.workspace.domain;

import com.businesslab.identity.domain.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkspaceMembershipTest {

    private static final Instant CREATED_AT =
        Instant.parse("2026-11-11T10:00:00Z");

    private static final Instant UPDATED_AT =
        Instant.parse("2026-11-11T11:00:00Z");

    @Test
    void shouldCreateActiveOwnerMembership() {
        WorkspaceId workspaceId = WorkspaceId.generate();
        UserId userId = UserId.generate();

        WorkspaceMembership membership =
            WorkspaceMembership.createOwner(
                workspaceId,
                userId,
                CREATED_AT
            );

        assertThat(membership.workspaceId()).isEqualTo(workspaceId);
        assertThat(membership.userId()).isEqualTo(userId);
        assertThat(membership.role())
            .isEqualTo(WorkspaceMembershipRole.OWNER);
        assertThat(membership.status())
            .isEqualTo(WorkspaceMembershipStatus.ACTIVE);
        assertThat(membership.canReadWorkspace()).isTrue();
        assertThat(membership.canCreateBusinesses()).isTrue();
        assertThat(membership.canManageMembers()).isTrue();
    }

    @Test
    void shouldCreateNonOwnerMembership() {
        WorkspaceMembership membership =
            WorkspaceMembership.createMember(
                WorkspaceId.generate(),
                UserId.generate(),
                WorkspaceMembershipRole.MEMBER,
                CREATED_AT
            );

        assertThat(membership.role())
            .isEqualTo(WorkspaceMembershipRole.MEMBER);
        assertThat(membership.canCreateBusinesses()).isTrue();
        assertThat(membership.canManageMembers()).isFalse();
    }

    @Test
    void shouldNotCreateOwnerThroughMemberFactory() {
        assertThatThrownBy(
            () -> WorkspaceMembership.createMember(
                WorkspaceId.generate(),
                UserId.generate(),
                WorkspaceMembershipRole.OWNER,
                CREATED_AT
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(
                "Owner membership must be created through createOwner"
            );
    }

    @Test
    void shouldEnforceViewerReadOnlyPermissions() {
        WorkspaceMembership membership =
            WorkspaceMembership.createMember(
                WorkspaceId.generate(),
                UserId.generate(),
                WorkspaceMembershipRole.VIEWER,
                CREATED_AT
            );

        assertThat(membership.canReadWorkspace()).isTrue();
        assertThat(membership.canCreateBusinesses()).isFalse();
        assertThat(membership.canManageMembers()).isFalse();
    }

    @Test
    void shouldChangeRoleOfActiveNonOwnerMembership() {
        WorkspaceMembership membership =
            WorkspaceMembership.createMember(
                WorkspaceId.generate(),
                UserId.generate(),
                WorkspaceMembershipRole.MEMBER,
                CREATED_AT
            );

        WorkspaceMembership updated = membership.changeRole(
            WorkspaceMembershipRole.ADMIN,
            UPDATED_AT
        );

        assertThat(updated.role())
            .isEqualTo(WorkspaceMembershipRole.ADMIN);
        assertThat(updated.updatedAt()).isEqualTo(UPDATED_AT);
        assertThat(membership.role())
            .isEqualTo(WorkspaceMembershipRole.MEMBER);
    }

    @Test
    void shouldNotAllowChangingMembershipToOwner() {
        WorkspaceMembership membership =
            WorkspaceMembership.createMember(
                WorkspaceId.generate(),
                UserId.generate(),
                WorkspaceMembershipRole.MEMBER,
                CREATED_AT
            );

        assertThatThrownBy(
            () -> membership.changeRole(
                WorkspaceMembershipRole.OWNER,
                UPDATED_AT
            )
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(
                "Ownership changes require a dedicated transfer operation"
            );
    }

    @Test
    void shouldSuspendAndReactivateMembership() {
        WorkspaceMembership membership =
            WorkspaceMembership.createMember(
                WorkspaceId.generate(),
                UserId.generate(),
                WorkspaceMembershipRole.MEMBER,
                CREATED_AT
            );

        WorkspaceMembership suspended =
            membership.suspend(UPDATED_AT);

        assertThat(suspended.status())
            .isEqualTo(WorkspaceMembershipStatus.SUSPENDED);
        assertThat(suspended.canReadWorkspace()).isFalse();

        WorkspaceMembership reactivated =
            suspended.reactivate(UPDATED_AT.plusSeconds(60));

        assertThat(reactivated.status())
            .isEqualTo(WorkspaceMembershipStatus.ACTIVE);
        assertThat(reactivated.canReadWorkspace()).isTrue();
    }

    @Test
    void shouldRemoveAndReactivateMembership() {
        WorkspaceMembership membership =
            WorkspaceMembership.createMember(
                WorkspaceId.generate(),
                UserId.generate(),
                WorkspaceMembershipRole.MEMBER,
                CREATED_AT
            );

        WorkspaceMembership removed = membership.remove(UPDATED_AT);

        assertThat(removed.status())
            .isEqualTo(WorkspaceMembershipStatus.REMOVED);
        assertThat(removed.canReadWorkspace()).isFalse();

        WorkspaceMembership reactivated =
            removed.reactivate(UPDATED_AT.plusSeconds(60));

        assertThat(reactivated.status())
            .isEqualTo(WorkspaceMembershipStatus.ACTIVE);
    }

    @Test
    void shouldPreventOwnerLifecycleChanges() {
        WorkspaceMembership owner =
            WorkspaceMembership.createOwner(
                WorkspaceId.generate(),
                UserId.generate(),
                CREATED_AT
            );

        assertThatThrownBy(() -> owner.suspend(UPDATED_AT))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage(
                "Owner membership cannot be modified through member lifecycle operations"
            );

        assertThatThrownBy(() -> owner.remove(UPDATED_AT))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage(
                "Owner membership cannot be modified through member lifecycle operations"
            );

        assertThatThrownBy(
            () -> owner.changeRole(
                WorkspaceMembershipRole.ADMIN,
                UPDATED_AT
            )
        )
            .isInstanceOf(IllegalStateException.class)
            .hasMessage(
                "Owner membership cannot be modified through member lifecycle operations"
            );
    }

    @Test
    void shouldRejectTimestampBeforePreviousUpdate() {
        WorkspaceMembership membership =
            WorkspaceMembership.createMember(
                WorkspaceId.generate(),
                UserId.generate(),
                WorkspaceMembershipRole.MEMBER,
                CREATED_AT
            );

        assertThatThrownBy(
            () -> membership.suspend(CREATED_AT.minusSeconds(1))
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(
                "Update timestamp cannot be before the previous update"
            );
    }
}
