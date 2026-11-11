"use client";

import Link from "next/link";
import {
  Suspense,
  useEffect,
  useMemo,
  useState,
  type FormEvent,
} from "react";
import { useRouter, useSearchParams } from "next/navigation";

import { businessApi } from "@/features/businesses/business-api";
import { useWorkspaces } from "@/features/workspaces/workspace-provider";
import { ApiRequestError } from "@/lib/api/client";
import { templateApi } from "@/lib/templates/template-api";
import type {
  BusinessTemplate,
  TemplateConfigurationField,
} from "@/lib/templates/template-types";

type ConfigurationValue = string | boolean;

type ConfigurationState = Record<string, ConfigurationValue>;

interface BusinessSetupDraft {
  templateKey: string;
  businessName: string;
  description: string;
  configuration: ConfigurationState;
}

function getInitialValues(
  template: BusinessTemplate,
): ConfigurationState {
  const values: ConfigurationState = {};

  for (const field of template.configurationFields) {
    if (field.type === "BOOLEAN") {
      values[field.key] =
        field.example?.toLowerCase() === "true";
    } else {
      values[field.key] = field.example ?? "";
    }
  }

  return values;
}

function buildConfiguration(
  template: BusinessTemplate,
  values: ConfigurationState,
): Record<string, unknown> {
  const configuration: Record<string, unknown> = {};

  for (const field of template.configurationFields) {
    const value = values[field.key];

    if (field.type === "BOOLEAN") {
      configuration[field.key] = value === true;
      continue;
    }

    if (typeof value !== "string") {
      continue;
    }

    const trimmed = value.trim();

    if (trimmed === "") {
      if (field.required) {
        throw new Error(`${field.label} is required.`);
      }

      continue;
    }

    if (field.type === "STRING") {
      configuration[field.key] = trimmed;
      continue;
    }

    const numericValue = Number(trimmed);

    if (!Number.isFinite(numericValue)) {
      throw new Error(`${field.label} must be a valid number.`);
    }

    if (field.type === "INTEGER" && !Number.isInteger(numericValue)) {
      throw new Error(`${field.label} must be a whole number.`);
    }

    if (
      field.minimum !== null &&
      field.minimum !== undefined &&
      numericValue < field.minimum
    ) {
      throw new Error(
        `${field.label} must be at least ${field.minimum}.`,
      );
    }

    if (
      field.maximum !== null &&
      field.maximum !== undefined &&
      numericValue > field.maximum
    ) {
      throw new Error(
        `${field.label} cannot exceed ${field.maximum}.`,
      );
    }

    configuration[field.key] = numericValue;
  }

  return configuration;
}

function ConfigurationField({
  field,
  value,
  onChange,
}: {
  field: TemplateConfigurationField;
  value: ConfigurationValue | undefined;
  onChange: (value: ConfigurationValue) => void;
}) {
  const id = `configuration-${field.key}`;

  if (field.type === "BOOLEAN") {
    return (
      <label className="bl-configuration-toggle" htmlFor={id}>
        <input
          id={id}
          type="checkbox"
          checked={value === true}
          onChange={(event) => onChange(event.target.checked)}
        />

        <span className="bl-toggle-copy">
          <strong>{field.label}</strong>
          <small>{field.description}</small>
        </span>
      </label>
    );
  }

  const inputType =
    field.type === "INTEGER" || field.type === "DECIMAL"
      ? "number"
      : "text";

  return (
    <label className="bl-form-field" htmlFor={id}>
      <span>
        {field.label}
        {field.required && <i aria-hidden="true"> *</i>}
      </span>

      <input
        id={id}
        type={inputType}
        value={typeof value === "string" ? value : ""}
        onChange={(event) => onChange(event.target.value)}
        placeholder={field.example ?? ""}
        required={field.required}
        maxLength={
          field.type === "STRING"
            ? field.maxLength ?? undefined
            : undefined
        }
        min={
          field.type === "INTEGER" || field.type === "DECIMAL"
            ? field.minimum ?? undefined
            : undefined
        }
        max={
          field.type === "INTEGER" || field.type === "DECIMAL"
            ? field.maximum ?? undefined
            : undefined
        }
        step={
          field.type === "INTEGER"
            ? 1
            : field.type === "DECIMAL"
              ? "any"
              : undefined
        }
        pattern={
          field.type === "STRING"
            ? field.pattern ?? undefined
            : undefined
        }
      />

      <small>
        {field.description}
        {!field.required && " · Optional"}
      </small>
    </label>
  );
}

function CreateBusinessLoading() {
  return (
    <div className="bl-page">
      <div className="bl-loading-card">
        Preparing business setup...
      </div>
    </div>
  );
}

function CreateBusinessForm() {
  const router = useRouter();
  const searchParams = useSearchParams();

  const {
    activeWorkspace,
    loading: workspacesLoading,
  } = useWorkspaces();

  const [templates, setTemplates] = useState<BusinessTemplate[]>([]);
  const [loadingTemplate, setLoadingTemplate] = useState(true);
  const [templateError, setTemplateError] = useState<string | null>(null);

  const [draftState, setDraftState] =
    useState<BusinessSetupDraft | null>(null);

  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const templateId = searchParams.get("templateId") ?? "";
  const templateVersion = searchParams.get("version") ?? "";

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
              : "Unable to load the selected template.",
          );
        }
      })
      .finally(() => {
        if (active) {
          setLoadingTemplate(false);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  const selectedTemplate = useMemo(
    () =>
      templates.find(
        (template) =>
          template.templateId === templateId &&
          template.version === templateVersion,
      ) ?? null,
    [templates, templateId, templateVersion],
  );

  const templateKey = selectedTemplate
    ? `${selectedTemplate.templateId}@${selectedTemplate.version}`
    : "";

  /*
   * The draft is derived from the currently selected template.
   * Switching template versions automatically selects fresh defaults.
   * No effect is required to copy template values into form state.
   */
  const draft = useMemo<BusinessSetupDraft | null>(() => {
    if (!selectedTemplate) {
      return null;
    }

    if (draftState?.templateKey === templateKey) {
      return draftState;
    }

    return {
      templateKey,
      businessName: `My ${selectedTemplate.name}`,
      description: "",
      configuration: getInitialValues(selectedTemplate),
    };
  }, [selectedTemplate, templateKey, draftState]);

  const canCreate =
    activeWorkspace !== null &&
    activeWorkspace.membershipRole !== "VIEWER" &&
    activeWorkspace.status === "ACTIVE";

  function updateDraft(
    updates: Partial<Omit<BusinessSetupDraft, "templateKey">>,
  ) {
    if (!draft) {
      return;
    }

    setDraftState({
      ...draft,
      ...updates,
    });
  }

  function updateConfiguration(
    key: string,
    value: ConfigurationValue,
  ) {
    if (!draft) {
      return;
    }

    setDraftState({
      ...draft,
      configuration: {
        ...draft.configuration,
        [key]: value,
      },
    });
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!activeWorkspace || !selectedTemplate || !draft || !canCreate) {
      return;
    }

    setSubmitting(true);
    setSubmitError(null);

    try {
      const configurationValues = buildConfiguration(
        selectedTemplate,
        draft.configuration,
      );

      await businessApi.create(
        activeWorkspace.id,
        {
          templateId: selectedTemplate.templateId,
          templateVersion: selectedTemplate.version,
          name: draft.businessName.trim(),
          description: draft.description.trim(),
          configuration: configurationValues,
        },
      );

      router.push("/app/businesses");
    } catch (requestError) {
      const message =
        requestError instanceof ApiRequestError
          ? requestError.details?.length
            ? requestError.details
                .map((detail) => {
                  const field = detail.field
                    ? `${detail.field}: `
                    : "";

                  return `${field}${detail.message ?? detail.code ?? "Invalid value"}`;
                })
                .join(" ")
            : requestError.message
          : requestError instanceof Error
            ? requestError.message
            : "Unable to create the business.";

      setSubmitError(message);
    } finally {
      setSubmitting(false);
    }
  }

  if (workspacesLoading || loadingTemplate) {
    return <CreateBusinessLoading />;
  }

  if (!activeWorkspace) {
    return (
      <div className="bl-page">
        <section className="bl-empty-panel">
          <span className="bl-eyebrow">BUSINESS SETUP</span>
          <h1>Create a workspace first.</h1>
          <p>
            A business must belong to a workspace. Create one before
            configuring your first business.
          </p>
          <Link href="/app" className="bl-primary-button">
            Set up workspace
          </Link>
        </section>
      </div>
    );
  }

  if (!canCreate) {
    return (
      <div className="bl-page">
        <section className="bl-empty-panel">
          <span className="bl-eyebrow">BUSINESS SETUP</span>
          <h1>Business creation is unavailable.</h1>
          <p>
            The selected workspace is either archived or your current
            role does not permit business creation.
          </p>
          <Link href="/app/discover" className="bl-secondary-button">
            Return to templates
          </Link>
        </section>
      </div>
    );
  }

  if (templateError) {
    return (
      <div className="bl-page">
        <div className="bl-error" role="alert">
          <strong>Template information is unavailable.</strong>
          <p>{templateError}</p>
          <Link href="/app/discover" className="bl-secondary-button">
            Return to template library
          </Link>
        </div>
      </div>
    );
  }

  if (!selectedTemplate || selectedTemplate.status !== "ACTIVE" || !draft) {
    return (
      <div className="bl-page">
        <section className="bl-empty-panel">
          <span className="bl-eyebrow">BUSINESS SETUP</span>
          <h1>Template not available.</h1>
          <p>
            The selected template version could not be found or is
            no longer available for new businesses.
          </p>
          <Link href="/app/discover" className="bl-primary-button">
            Explore templates
          </Link>
        </section>
      </div>
    );
  }

  return (
    <div className="bl-page">
      <header className="bl-page-heading">
        <div>
          <span className="bl-eyebrow">
            CREATE A NEW BUSINESS
          </span>

          <h1>Configure your business.</h1>

          <p>
            You are creating this business in{" "}
            <strong>{activeWorkspace.name}</strong>.
          </p>
        </div>

        <Link href="/app/discover" className="bl-secondary-button">
          Back to templates
        </Link>
      </header>

      <section className="bl-selected-template">
        <div className="bl-business-symbol">
          {selectedTemplate.name.charAt(0)}
        </div>

        <div>
          <span className="bl-eyebrow">SELECTED TEMPLATE</span>
          <h2>{selectedTemplate.name}</h2>
          <p>{selectedTemplate.description}</p>
          <small>
            {selectedTemplate.templateId} · Version{" "}
            {selectedTemplate.version}
          </small>
        </div>
      </section>

      <form
        className="bl-creation-form"
        onSubmit={handleSubmit}
      >
        <section className="bl-form-section">
          <div className="bl-form-section-heading">
            <span>01</span>
            <div>
              <h2>Business details</h2>
              <p>Give your virtual business an identity.</p>
            </div>
          </div>

          <div className="bl-form-grid">
            <label className="bl-form-field">
              <span>Business name *</span>
              <input
                type="text"
                value={draft.businessName}
                onChange={(event) =>
                  updateDraft({
                    businessName: event.target.value,
                  })
                }
                maxLength={120}
                required
                placeholder="Enter a business name"
              />
              <small>
                This is the name displayed in your portfolio.
              </small>
            </label>

            <label className="bl-form-field">
              <span>Description</span>
              <textarea
                value={draft.description}
                onChange={(event) =>
                  updateDraft({
                    description: event.target.value,
                  })
                }
                maxLength={1000}
                rows={3}
                placeholder="Describe your business idea"
              />
              <small>
                A short description of what you want to build.
              </small>
            </label>
          </div>
        </section>

        <section className="bl-form-section">
          <div className="bl-form-section-heading">
            <span>02</span>
            <div>
              <h2>Initial configuration</h2>
              <p>
                These fields are defined by the selected template.
                The template validates the submitted values.
              </p>
            </div>
          </div>

          {selectedTemplate.configurationFields.length === 0 ? (
            <div className="bl-muted-panel">
              This template does not require additional configuration.
            </div>
          ) : (
            <div className="bl-form-grid">
              {selectedTemplate.configurationFields.map((field) => (
                <ConfigurationField
                  key={field.key}
                  field={field}
                  value={draft.configuration[field.key]}
                  onChange={(value) =>
                    updateConfiguration(field.key, value)
                  }
                />
              ))}
            </div>
          )}
        </section>

        {submitError && (
          <div className="bl-error" role="alert">
            <strong>Business creation failed.</strong>
            <p>{submitError}</p>
          </div>
        )}

        <div className="bl-form-actions">
          <Link href="/app/discover" className="bl-secondary-button">
            Cancel
          </Link>

          <button
            type="submit"
            className="bl-primary-button"
            disabled={submitting || !draft.businessName.trim()}
          >
            {submitting
              ? "Creating business..."
              : "Create business"}
            {!submitting && <span aria-hidden="true">↗</span>}
          </button>
        </div>
      </form>
    </div>
  );
}

export default function CreateBusinessPage() {
  return (
    <Suspense fallback={<CreateBusinessLoading />}>
      <CreateBusinessForm />
    </Suspense>
  );
}
