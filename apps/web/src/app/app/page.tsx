
"use client";

import Link from "next/link";
import { useEffect, useState } from "react";

import { useAuth } from "@/features/auth/auth-provider";
import { templateApi } from "@/lib/templates/template-api";
import type { BusinessTemplate } from "@/lib/templates/template-types";

export default function BusinessLabOverviewPage() {
  const { user } = useAuth();

  const [templates, setTemplates] = useState<BusinessTemplate[]>([]);
  const [loadingTemplates, setLoadingTemplates] = useState(true);
  const [templateError, setTemplateError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;

    void templateApi.list()
      .then((result) => {
        if (active) {
          setTemplates(result);
          setTemplateError(null);
        }
      })
      .catch((error: unknown) => {
        if (active) {
          setTemplateError(
            error instanceof Error
              ? error.message
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

  const firstName = user?.displayName.split(" ")[0] ?? "there";

  return (
    <div className="overview-page">
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
              Explore business templates <span aria-hidden="true">↗</span>
            </Link>
            <span className="overview-action-note">
              Start with an idea. Learn by doing.
            </span>
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

      <section className="overview-metrics" aria-label="Environment summary">
        <article className="overview-metric-card">
          <span className="metric-label">AVAILABLE TEMPLATES</span>
          <strong>
            {loadingTemplates ? "—" : templates.length}
          </strong>
          <span className="metric-caption">
            {templateError ? "Catalog unavailable" : "Business models to explore"}
          </span>
        </article>

        <article className="overview-metric-card">
          <span className="metric-label">YOUR BUSINESSES</span>
          <strong className="metric-empty-value">Not started</strong>
          <span className="metric-caption">
            Your business portfolio starts with your first creation
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
            Explore all templates <span aria-hidden="true">↗</span>
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

          <article className="overview-start-card">
            <span className="start-card-number">02 / CREATE</span>
            <div className="start-card-illustration start-illustration-create">
              <span className="create-square create-square-one" />
              <span className="create-square create-square-two" />
              <span className="create-square create-square-three" />
            </div>
            <div>
              <h3>Build your first business</h3>
              <p>
                Configure your business and establish the
                foundations for its journey.
              </p>
              <span className="start-card-status">SETUP EXPERIENCE NEXT</span>
            </div>
          </article>

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
              <span className="start-card-status">EXPERIMENT LAB PLANNED</span>
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
          <span className="overview-muted-label">READY WHEN YOU ARE</span>
        </div>

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
            <Link href="/app/discover" className="app-text-link">
              Find a business to build <span aria-hidden="true">↗</span>
            </Link>
          </div>
        </div>
      </section>

      {templateError && (
        <p className="overview-inline-error" role="status">
          Template discovery is temporarily unavailable: {templateError}
        </p>
      )}
    </div>
  );
}
