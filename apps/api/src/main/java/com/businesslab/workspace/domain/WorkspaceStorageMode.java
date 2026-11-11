package com.businesslab.workspace.domain;

/**
 * Defines where a workspace's business data is persisted.
 *
 * LOCAL:
 * Data is stored in the user's browser using IndexedDB.
 *
 * CLOUD:
 * Data is stored by BusinessLab on the server.
 */
public enum WorkspaceStorageMode {

    LOCAL,

    CLOUD
}
