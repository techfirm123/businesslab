"use client";

import Link from "next/link";
import {
  useCallback,
  useEffect,
  useState,
  type FormEvent,
} from "react";

import { useAuth } from "@/features/auth/auth-provider";
import { businessApi } from "@/features/businesses/business-api";
import type { Business } from "@/features/businesses/business-types";
import { useWorkspaces } from "@/features/workspaces/workspace-provider";
import { templateApi } from "@/lib/templates/template-api";
import type { BusinessTemplate } from "@/lib/templates/template-types";

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

interface WorkspaceFormProps {
  onSubmit: (name: string) => Promise<void>;
  submitting: boolean;
  error: string | null;
  initialName?: string;
  submitLabel?: string;
}

function WorkspaceForm({
  onSubmit,
  submitting,
  error,
  initialName = "",
  submitLabel = "Create workspace",
}: WorkspaceFormProps) {
  const [name, setName] = useState(initialName);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!name.trim()) {
      return;
    }

    try {
      await onSubmit(name.trim());
      setName("");
    } catch {
      // The parent displays the request error. Keep the name for retry.
    }
  }

  return (
    <form className="bl-workspace-form" onSubmit={handleSubmit}>
      <label htmlFor="workspace-name">
        Workspace name
      </label>

      <input
        id="workspace-name"
        type="text"
        value={name}
        onChange={(event) => setName(event.target.value)}
        placeholder="For example, My Business Ideas"
        maxLength={100}
        minLength={1}
        required
        autoComplete="off"
      />

      <small>
        A workspace keeps your businesses and experiments organised.
      </small>

      {error && (
        <p className="bl-inline-error" role="alert">
          {error}
        </p>
      )}

      <button
        type="submit"
        className="bl-primary-button"
        disabled={submitting || !name.trim()}
      >
        {submitting ? "Creating workspace..." : submitLabel}
      </button>
    </form>
  );
}

export default function BusinessLabOverviewPage() {
  const { user } = useAuth();

  const {
    workspaces,
    activeWorkspace,
    loading: workspacesLoading,
    error: workspaceError,
    creating: creatingWorkspace,
    createWorkspace,
    refreshWorkspaces,
  } = useWorkspaces();

  const [templates, setTemplates] = useState<BusinessTemplate[]>([]);
  const [loadingTemplates, setLoadingTemplates] = useState(true);
  const [templateError, setTemplateError] = useState<string | null>(null);

  const [businesses, setBusinesses] = useState<Business[]>([]);
  const [loadingBusinesses, setLoadingBusinesses] = useState(false);
  const [businessError, setBusinessError] = useState<string | null>(null);

  const [workspaceCreateError, setWorkspaceCreateError] =
    useState<string | null>(null);

  const firstName = user?.displayName.split(" ")[0] ?? "there";

  useEffect(() => {
    let active = true;

    void templateApi.list()
      .then((result) => {
        if (active) {
          setTemplates(result);
          setTemplateError(null);
        }
      })
      .catch((requestError: unknown) => {
        if (active) {
          setTemplateError(
            requestError instanceof Error
              ? requestError.message
              : "Unable to load business templates.",
          );
        }
      })
      .finally(() => {
        if (active) {
          setLoadingTemplates(false);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  const loadBusinesses = useCallback(async () => {
    if (!activeWorkspace) {
      setBusinesses([]);
      setLoadingBusinesses(false);
      setBusinessError(null);
      return;
    }

    setLoadingBusinesses(true);
    setBusinessError(null);

    try {
      const result = await businessApi.list(activeWorkspace.id);
      setBusinesses(result);
    } catch (requestError) {
      setBusinessError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to load businesses.",
      );
    } finally {
      setLoadingBusinesses(false);
    }
  }, [activeWorkspace]);

  useEffect(() => {
    if (!activeWorkspace) {
      return;
    }

    let active = true;

    void businessApi.list(activeWorkspace.id)
      .then((result) => {
        if (active) {
          setBusinesses(result);
          setBusinessError(null);
        }
      })
      .catch((requestError: unknown) => {
        if (active) {
          setBusinessError(
            requestError instanceof Error
              ? requestError.message
              : "Unable to load businesses.",
          );
        }
      })
      .finally(() => {
        if (active) {
          setLoadingBusinesses(false);
        }
      });

    return () => {
      active = false;
    };
  }, [activeWorkspace]);

  async function handleCreateWorkspace(name: string): Promise<void> {
    setWorkspaceCreateError(null);

    try {
      await createWorkspace(name);
    } catch (requestError) {
      setWorkspaceCreateError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to create the workspace.",
      );

      throw requestError;
    }
  }

  if (workspacesLoading) {
    return (
      <div className="bl-page">
        <div className="bl-loading-card">
          Preparing your workspace...
        </div>
      </div>
    );
  }

  if (workspaceError && workspaces.length === 0) {
    return (
      <div className="bl-page">
        <div className="bl-error" role="alert">
          <strong>We couldn&apos;t load your workspaces.</strong>
          <p>{workspaceError}</p>
          <button
            type="button"
            className="bl-secondary-button"
            onClick={() => void refreshWorkspaces()}
          >
            Try again
          </button>
        </div>
      </div>
    );
  }

  if (workspaces.length === 0) {
    return (
      <div className="bl-page">
        <section className="bl-onboarding-hero">
          <div className="bl-onboarding-copy">
            <span className="bl-eyebrow">
              YOUR BUSINESSLAB JOURNEY
            </span>

            <h1>
              Welcome, {firstName}.
              <br />
              <span>Let&apos;s build your first business world.</span>
            </h1>

            <p>
              Start by creating a workspace. It will be the home
              for your virtual businesses, decisions and experiments.
            </p>

            <div className="bl-onboarding-steps">
              <div className="bl-onboarding-step active">
                <span>01</span>
                <div>
                  <strong>Create a workspace</strong>
                  <small>Set up your business environment</small>
                </div>
              </div>

              <div className="bl-onboarding-step">
                <span>02</span>
                <div>
                  <strong>Choose a business model</strong>
                  <small>Explore the template library</small>
                </div>
              </div>

              <div className="bl-onboarding-step">
                <span>03</span>
                <div>
                  <strong>Create your business</strong>
                  <small>Configure its starting conditions</small>
                </div>
              </div>
            </div>
          </div>

          <div className="bl-onboarding-card">
            <span className="bl-eyebrow">
              STEP 01 / WORKSPACE SETUP
            </span>

            <h2>Create your workspace.</h2>

            <p>
              Give your environment a name. You can create additional
              workspaces later and switch between them.
            </p>

            <WorkspaceForm
              onSubmit={handleCreateWorkspace}
              submitting={creatingWorkspace}
              error={workspaceCreateError}
              initialName="My Business World"
            />
          </div>
        </section>
      </div>
    );
  }

  if (!activeWorkspace) {
    return (
      <div className="bl-page">
        <div className="bl-error" role="alert">
          <strong>Unable to select a workspace.</strong>
          <p>
            Refresh your workspace list and try again.
          </p>
          <button
            type="button"
            className="bl-secondary-button"
            onClick={() => void refreshWorkspaces()}
          >
            Refresh workspaces
          </button>
        </div>
      </div>
    );
  }

  const activeTemplates = templates.filter(
    (template) => template.status === "ACTIVE",
  );

  const canCreateBusiness =
    activeWorkspace.membershipRole !== "VIEWER" &&
    activeWorkspace.status === "ACTIVE";

  return (
    <div className="overview-page bl-page">
      <section className="overview-welcome">
        <div className="overview-welcome-copy">
          <span className="overview-eyebrow">
            YOUR BUSINESSLAB ENVIRONMENT
          </span>

          <h1>
            Welcome back, {firstName}.
            <br />
            <span>What will you build today?</span>
          </h1>

          <p>
            Every business begins with a question. Explore an idea,
            discover a business model and learn by experimenting.
          </p>

          <div className="overview-actions">
            <Link href="/app/discover" className="app-primary-button">
              Explore business templates
              <span aria-hidden="true">↗</span>
            </Link>

            <Link href="/app/businesses" className="app-text-link">
              View your businesses
            </Link>
          </div>
        </div>

        <div className="overview-world-card" aria-hidden="true">
          <div className="overview-world-top">
            <span>BUSINESSLAB / WORLD 001</span>
            <span>YOUR JOURNEY</span>
          </div>

          <div className="overview-world">
            <div className="overview-orbit overview-orbit-one" />
            <div className="overview-orbit overview-orbit-two" />
            <div className="overview-orbit overview-orbit-three" />

            <div className="overview-world-center">B</div>

            <span className="overview-world-point point-one">
              <i />
              BUILD
            </span>

            <span className="overview-world-point point-two">
              <i />
              OPERATE
            </span>

            <span className="overview-world-point point-three">
              <i />
              EXPERIMENT
            </span>
          </div>

          <div className="overview-world-footer">
            <span>ONE WORLD</span>
            <strong>MANY POSSIBILITIES</strong>
          </div>
        </div>
      </section>

      <section className="bl-active-workspace-panel">
        <div>
          <span className="bl-eyebrow">ACTIVE WORKSPACE</span>
          <h2>{activeWorkspace.name}</h2>
          <p>
            {activeWorkspace.membershipRole} · {activeWorkspace.status}
          </p>
        </div>

        <details className="bl-add-workspace">
          <summary>Create another workspace</summary>
          <WorkspaceForm
            onSubmit={handleCreateWorkspace}
            submitting={creatingWorkspace}
            error={workspaceCreateError}
            submitLabel="Create and switch"
          />
        </details>
      </section>

      <section className="overview-metrics" aria-label="Workspace summary">
        <article className="overview-metric-card">
          <span className="metric-label">AVAILABLE TEMPLATES</span>
          <strong>{loadingTemplates ? "—" : activeTemplates.length}</strong>
          <span className="metric-caption">
            {templateError
              ? "Catalog unavailable"
              : "Active business models"}
          </span>
        </article>

        <article className="overview-metric-card">
          <span className="metric-label">YOUR BUSINESSES</span>
          <strong>
            {loadingBusinesses ? "—" : businesses.length}
          </strong>
          <span className="metric-caption">
            {businessError
              ? "Portfolio unavailable"
              : "Businesses in this workspace"}
          </span>
        </article>

        <article className="overview-metric-card">
          <span className="metric-label">EXPERIMENTS</span>
          <strong className="metric-empty-value">Coming soon</strong>
          <span className="metric-caption">
            Compare decisions and discover outcomes
          </span>
        </article>
      </section>

      <section className="overview-section">
        <div className="overview-section-heading">
          <div>
            <span className="overview-eyebrow">GET STARTED</span>
            <h2>Find your starting point.</h2>
          </div>

          <Link href="/app/discover" className="app-text-link">
            Explore all templates
            <span aria-hidden="true">↗</span>
          </Link>
        </div>

        <div className="overview-start-grid">
          <Link
            href="/app/discover"
            className="overview-start-card start-card-featured"
          >
            <span className="start-card-number">01 / DISCOVER</span>

            <div className="start-card-illustration start-illustration-discover">
              <span className="illustration-orbit orbit-a" />
              <span className="illustration-orbit orbit-b" />
              <span className="illustration-core">B</span>
            </div>

            <div>
              <h3>Explore business models</h3>
              <p>
                Discover different industries, business concepts
                and ways to get started.
              </p>
            </div>

            <span className="start-card-arrow">↗</span>
          </Link>

          {canCreateBusiness ? (
            <Link
              href="/app/discover"
              className="overview-start-card"
            >
              <span className="start-card-number">02 / CREATE</span>

              <div className="start-card-illustration start-illustration-create">
                <span className="create-square create-square-one" />
                <span className="create-square create-square-two" />
                <span className="create-square create-square-three" />
              </div>

              <div>
                <h3>Build your business</h3>
                <p>
                  Select a template, provide its starting configuration
                  and create your virtual business.
                </p>
              </div>

              <span className="start-card-arrow">↗</span>
            </Link>
          ) : (
            <article className="overview-start-card">
              <span className="start-card-number">02 / CREATE</span>

              <div>
                <h3>Business creation is restricted</h3>
                <p>
                  Your current workspace role does not permit business
                  creation.
                </p>
              </div>
            </article>
          )}

          <article className="overview-start-card">
            <span className="start-card-number">03 / EXPERIMENT</span>

            <div className="start-card-illustration start-illustration-experiment">
              <span className="experiment-line experiment-line-one" />
              <span className="experiment-line experiment-line-two" />
              <span className="experiment-line experiment-line-three" />
            </div>

            <div>
              <h3>Learn through decisions</h3>
              <p>
                Test strategies, compare scenarios and understand
                the consequences of business decisions.
              </p>

              <span className="start-card-status">
                EXPERIMENT LAB PLANNED
              </span>
            </div>
          </article>
        </div>
      </section>

      <section className="overview-section overview-bottom-section">
        <div className="overview-section-heading">
          <div>
            <span className="overview-eyebrow">YOUR JOURNEY</span>
            <h2>Your business portfolio.</h2>
          </div>

          <Link href="/app/businesses" className="app-text-link">
            View all businesses
            <span aria-hidden="true">↗</span>
          </Link>
        </div>

        {businessError && (
          <div className="bl-error" role="alert">
            {businessError}
            <button
              type="button"
              className="bl-secondary-button"
              onClick={() => void loadBusinesses()}
            >
              Retry
            </button>
          </div>
        )}

        {loadingBusinesses && (
          <div className="bl-loading-card">
            Loading your businesses...
          </div>
        )}

        {!loadingBusinesses && !businessError && businesses.length === 0 && (
          <div className="overview-empty-state">
            <div className="empty-state-symbol">
              <span />
              <span />
              <span />
            </div>

            <div>
              <h3>Your first business is waiting to be imagined.</h3>
              <p>
                Choose a business model and start shaping the virtual
                world you want to explore.
              </p>

              {canCreateBusiness && (
                <Link href="/app/discover" className="app-text-link">
                  Find a business to build
                  <span aria-hidden="true">↗</span>
                </Link>
              )}
            </div>
          </div>
        )}

        {!loadingBusinesses && businesses.length > 0 && (
          <div className="bl-business-preview-list">
            {businesses.slice(0, 3).map((business) => (
              <article
                className="bl-business-preview"
                key={business.id}
              >
                <div className="bl-business-symbol">
                  {business.name.charAt(0).toUpperCase()}
                </div>

                <div>
                  <strong>{business.name}</strong>
                  <span>
                    {business.templateId} · {business.templateVersion}
                  </span>
                </div>

                <span className="bl-preview-date">
                  {formatDate(business.createdAt)}
                </span>
              </article>
            ))}

            {businesses.length > 3 && (
              <Link href="/app/businesses" className="app-text-link">
                View all {businesses.length} businesses
                <span aria-hidden="true">↗</span>
              </Link>
            )}
          </div>
        )}
      </section>

      {templateError && (
        <p className="overview-inline-error" role="status">
          Template discovery is temporarily unavailable: {templateError}
        </p>
      )}
    </div>
  );
}
