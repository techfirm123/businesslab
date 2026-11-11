
package com.businesslab.workspace.application;

import com.businesslab.identity.domain.UserId;
import com.businesslab.workspace.domain.Workspace;
import com.businesslab.workspace.domain.WorkspaceId;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port for workspaces.
 */
public interface WorkspaceRepository {

    void save(Workspace workspace);

    Optional<Workspace> findById(WorkspaceId workspaceId);

    List<Workspace> findAllForUser(UserId userId);
}
