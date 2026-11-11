
export interface TemplateConfigurationField {
  key: string;
  label: string;
  description: string;
  type: "STRING" | "INTEGER" | "DECIMAL" | "BOOLEAN";
  required: boolean;
  example?: string | null;
  minimum?: number | null;
  maximum?: number | null;
  maxLength?: number | null;
  pattern?: string | null;
}

export interface BusinessTemplate {
  templateId: string;
  version: string;
  sourceType: "CODE_BACKED" | "DECLARATIVE";
  name: string;
  description: string;
  category: string;
  status: "ACTIVE" | "DEPRECATED";
  capabilities: string[];
  tags: string[];
  configurationFields: TemplateConfigurationField[];
}
