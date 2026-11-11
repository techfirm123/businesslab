
"use client";

import { useEffect, useState } from "react";

import { ApiRequestError } from "@/lib/api/client";
import { templateApi } from "@/lib/templates/template-api";
import type {
  BusinessTemplate,
} from "@/lib/templates/template-types";

function formatCategory(category: string): string {
  return category
    .toLowerCase()
    .split("_")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
}

function formatFieldType(type: string): string {
  return type.charAt(0) + type.slice(1).toLowerCase();
}

function TemplateCard({
  template,
}: {
  template: BusinessTemplate;
}) {
  return (
    <article className="template-card">
      <div className="template-card-top">
        <div className="template-symbol">
          {template.name.charAt(0)}
        </div>

        <span className="template-status">
          <span className="auth-note-dot" />
          {template.status}
        </span>
      </div>

      <div className="template-category">
        {formatCategory(template.category)}
      </div>

      <div className="template-card-heading">
        <div>
          <h2>{template.name}</h2>
          <span className="template-version">
            VERSION {template.version}
          </span>
        </div>
      </div>

      <p className="template-description">
        {template.description}
      </p>

      <div className="template-tags">
        {template.tags.slice(0, 4).map((tag) => (
          <span key={tag}>{tag.replaceAll("-", " ")}</span>
        ))}
      </div>

      <div className="template-config">
        <span className="template-config-label">
          INITIAL CONFIGURATION
        </span>

        {template.configurationFields.map((field) => (
          <div className="template-config-row" key={field.key}>
            <div>
              <strong>{field.label}</strong>
              <span>{field.description}</span>
            </div>
            <small>{formatFieldType(field.type)}</small>
          </div>
        ))}
      </div>

      <div className="template-card-footer">
        <span>{template.capabilities.length} capabilities</span>
        <button type="button" disabled>
          Setup flow coming next
        </button>
      </div>
    </article>
  );
}

export default function DiscoverPage() {
  const [templates, setTemplates] = useState<BusinessTemplate[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;

    void templateApi.list()
      .then((result) => {
        if (active) {
          setTemplates(result);
          setError(null);
        }
      })
      .catch((requestError: unknown) => {
        if (!active) {
          return;
        }

        setError(
          requestError instanceof ApiRequestError
            ? requestError.message
            : "Unable to load business templates.",
        );
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  return (
    <div className="discover-page">
      <section className="discover-heading">
        <div>
          <span className="overview-eyebrow">THE TEMPLATE LIBRARY</span>
          <h1>Discover what you can build.</h1>
          <p>
            Explore business models across industries. Each template
            gives you a starting point for a new virtual business.
          </p>
        </div>

        <div className="discover-catalog-meta">
          <span>PLATFORM CATALOG</span>
          <strong>
            {loading ? "—" : String(templates.length).padStart(2, "0")}
          </strong>
          <small>AVAILABLE TEMPLATES</small>
        </div>
      </section>

      {loading && (
        <div className="template-loading-grid">
          <div className="template-loading-card" />
          <div className="template-loading-card" />
        </div>
      )}

      {error && (
        <div className="template-error" role="alert">
          <strong>We couldn&apos;t load the template library.</strong>
          <p>{error}</p>
          <button
            type="button"
            onClick={() => window.location.reload()}
          >
            Try again
          </button>
        </div>
      )}

      {!loading && !error && templates.length === 0 && (
        <div className="overview-empty-state">
          <div>
            <h3>No templates are currently available.</h3>
            <p>Please check back as the BusinessLab catalog develops.</p>
          </div>
        </div>
      )}

      {!loading && !error && templates.length > 0 && (
        <>
          <div className="discover-results-heading">
            <span>AVAILABLE BUSINESS MODELS</span>
            <span>{templates.length} RESULTS</span>
          </div>

          <div className="template-grid">
            {templates.map((template) => (
              <TemplateCard
                key={`${template.templateId}@${template.version}`}
                template={template}
              />
            ))}
          </div>
        </>
      )}
    </div>
  );
}
