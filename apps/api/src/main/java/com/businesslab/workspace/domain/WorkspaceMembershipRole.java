
package com.businesslab.workspace.domain;

/**
 * Defines the role assigned to a user within a workspace.
 */
public enum WorkspaceMembershipRole {

    OWNER,

    ADMIN,

    MEMBER,

    VIEWER;

    public boolean canManageMembers() {
        return this == OWNER || this == ADMIN;
    }

    public boolean canCreateBusinesses() {
        return this != VIEWER;
    }

    public boolean canReadWorkspace() {
        return true;
    }
}
