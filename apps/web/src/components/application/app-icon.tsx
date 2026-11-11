
import type { ReactNode } from "react";

interface AppIconProps {
  name: "overview" | "discover" | "business" | "experiment" | "studio";
  size?: number;
}

const paths: Record<AppIconProps["name"], ReactNode> = {
  overview: (
    <>
      <rect x="3" y="3" width="7" height="7" rx="1.5" />
      <rect x="14" y="3" width="7" height="7" rx="1.5" />
      <rect x="3" y="14" width="7" height="7" rx="1.5" />
      <rect x="14" y="14" width="7" height="7" rx="1.5" />
    </>
  ),
  discover: (
    <>
      <circle cx="10.8" cy="10.8" r="7.2" />
      <path d="m15.9 15.9 5 5" />
      <path d="m13.5 8.5-2.2 5-5 2.2 2.2-5 5-2.2Z" />
    </>
  ),
  business: (
    <>
      <path d="M3 21h18" />
      <path d="M5 21V7l7-4 7 4v14" />
      <path d="M9 21v-5h6v5" />
      <path d="M9 9h.01M15 9h.01M9 12h.01M15 12h.01" />
    </>
  ),
  experiment: (
    <>
      <path d="M9 3h6" />
      <path d="M10 3v6L4 19a2 2 0 0 0 1.7 3h12.6a2 2 0 0 0 1.7-3L14 9V3" />
      <path d="M8 16h8" />
      <path d="m9 13 2 2 3-3" />
    </>
  ),
  studio: (
    <>
      <rect x="3" y="3" width="7" height="5" rx="1" />
      <rect x="14" y="16" width="7" height="5" rx="1" />
      <rect x="14" y="3" width="7" height="5" rx="1" />
      <path d="M6.5 8v4h11V8M17.5 12v4" />
    </>
  ),
};

export function AppIcon({
  name,
  size = 19,
}: AppIconProps) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.7"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {paths[name]}
    </svg>
  );
}
