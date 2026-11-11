
package com.businesslab.business.application;

import com.businesslab.business.domain.Business;
import com.businesslab.business.domain.BusinessId;
import com.businesslab.workspace.domain.WorkspaceId;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port for businesses.
 */
public interface BusinessRepository {

    void save(Business business);

    Optional<Business> findById(BusinessId businessId);

    List<Business> findByWorkspaceId(WorkspaceId workspaceId);
}
