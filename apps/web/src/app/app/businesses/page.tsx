"use client";

import Link from "next/link";
import { useEffect, useState } from "react";

import { businessApi } from "@/features/businesses/business-api";
import type { Business } from "@/features/businesses/business-types";
import { useWorkspaces } from "@/features/workspaces/workspace-provider";

interface BusinessLoadState {
  workspaceId: string;
  requestKey: number;
  businesses: Business[];
  error: string | null;
}

function formatDate(value: string): string {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "Date unavailable";
  }

  return new Intl.DateTimeFormat("en-IN", {
    day: "numeric",
    month: "short",
    year: "numeric",
  }).format(date);
}

function formatStatus(status: string): string {
  return status
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (character) => character.toUpperCase());
}

export default function BusinessesPage() {
  const {
    activeWorkspace,
    loading: workspacesLoading,
  } = useWorkspaces();

  const workspaceId = activeWorkspace?.id ?? null;

  const [loadState, setLoadState] =
    useState<BusinessLoadState | null>(null);

  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    if (!workspaceId) {
      return;
    }

    let active = true;

    void businessApi.list(workspaceId)
      .then((result) => {
        if (!active) {
          return;
        }

        setLoadState({
          workspaceId,
          requestKey: reloadKey,
          businesses: result,
          error: null,
        });
      })
      .catch((requestError: unknown) => {
        if (!active) {
          return;
        }

        setLoadState({
          workspaceId,
          requestKey: reloadKey,
          businesses: [],
          error:
            requestError instanceof Error
              ? requestError.message
              : "Unable to load businesses for this workspace.",
        });
      });

    return () => {
      active = false;
    };
  }, [workspaceId, reloadKey]);

  const hasCurrentResponse =
    workspaceId !== null &&
    loadState?.workspaceId === workspaceId &&
    loadState.requestKey === reloadKey;

  const loading =
    workspacesLoading ||
    (workspaceId !== null && !hasCurrentResponse);

  const businesses = hasCurrentResponse
    ? loadState.businesses
    : [];

  const error = hasCurrentResponse
    ? loadState.error
    : null;

  const canCreate =
    activeWorkspace !== null &&
    activeWorkspace.membershipRole !== "VIEWER" &&
    activeWorkspace.status === "ACTIVE";

  if (loading) {
    return (
      <div className="bl-page">
        <div className="bl-loading-card">
          Loading your workspace...
        </div>
      </div>
    );
  }

  if (!activeWorkspace) {
    return (
      <div className="bl-page">
        <section className="bl-empty-panel">
          <span className="bl-eyebrow">YOUR PORTFOLIO</span>
          <h1>Create your first workspace.</h1>
          <p>
            Businesses belong to a workspace. Create one before
            starting your first virtual business.
          </p>
          <Link href="/app" className="bl-primary-button">
            Set up a workspace
          </Link>
        </section>
      </div>
    );
  }

  return (
    <div className="bl-page">
      <header className="bl-page-heading">
        <div>
          <span className="bl-eyebrow">YOUR BUSINESS PORTFOLIO</span>
          <h1>My businesses</h1>
          <p>
            Businesses in <strong>{activeWorkspace.name}</strong>.
            Each one is a persistent space for learning and experimentation.
          </p>
        </div>

        {canCreate && (
          <Link href="/app/discover" className="bl-primary-button">
            Create a business <span aria-hidden="true">↗</span>
          </Link>
        )}
      </header>

      <div className="bl-portfolio-summary">
        <div>
          <span>BUSINESS COUNT</span>
          <strong>{businesses.length}</strong>
        </div>

        <div>
          <span>WORKSPACE ROLE</span>
          <strong className="bl-summary-role">
            {activeWorkspace.membershipRole}
          </strong>
        </div>

        <div>
          <span>WORKSPACE STATUS</span>
          <strong className="bl-summary-role">
            {activeWorkspace.status}
          </strong>
        </div>
      </div>

      {error && (
        <div className="bl-error" role="alert">
          <strong>We couldn&apos;t load your businesses.</strong>
          <p>{error}</p>
          <button
            type="button"
            className="bl-secondary-button"
            onClick={() => setReloadKey((current) => current + 1)}
          >
            Try again
          </button>
        </div>
      )}

      {!error && businesses.length === 0 && (
        <section className="bl-empty-panel">
          <div className="bl-empty-symbol">
            <span />
            <span />
            <span />
          </div>

          <span className="bl-eyebrow">A NEW IDEA STARTS HERE</span>
          <h2>This workspace has no businesses yet.</h2>
          <p>
            Choose a business model from the template library,
            configure its starting conditions and create your first
            virtual business.
          </p>

          {canCreate ? (
            <Link href="/app/discover" className="bl-primary-button">
              Explore business templates
              <span aria-hidden="true">↗</span>
            </Link>
          ) : (
            <p className="bl-muted-note">
              Your VIEWER role allows you to explore existing businesses,
              but not create new ones.
            </p>
          )}
        </section>
      )}

      {!error && businesses.length > 0 && (
        <section className="bl-business-list-section">
          <div className="bl-section-heading">
            <div>
              <span className="bl-eyebrow">WORKSPACE CONTENT</span>
              <h2>Your businesses</h2>
            </div>

            <span className="bl-result-count">
              {businesses.length}{" "}
              {businesses.length === 1 ? "BUSINESS" : "BUSINESSES"}
            </span>
          </div>

          <div className="bl-business-grid">
            {businesses.map((business) => (
              <article className="bl-business-card" key={business.id}>
                <div className="bl-business-card-top">
                  <div className="bl-business-symbol">
                    {business.name.charAt(0).toUpperCase()}
                  </div>

                  <span className="bl-status-label">
                    {formatStatus(business.status)}
                  </span>
                </div>

                <div className="bl-business-card-content">
                  <span className="bl-business-template">
                    {business.templateId} · {business.templateVersion}
                  </span>

                  <h3>{business.name}</h3>

                  <p>
                    {business.description || "No description provided."}
                  </p>
                </div>

                <div className="bl-business-card-footer">
                  <span>CREATED {formatDate(business.createdAt)}</span>
                  <span className="bl-business-id">
                    {business.id.slice(0, 8).toUpperCase()}
                  </span>
                </div>
              </article>
            ))}
          </div>
        </section>
      )}
    </div>
  );
}
