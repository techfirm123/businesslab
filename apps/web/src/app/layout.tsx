import type { Metadata } from "next";
import type { ReactNode } from "react";

import "./globals.css";
import "./application.css";
import "./application-workspace.css";

export const metadata: Metadata = {
  title: {
    default: "BusinessLab | Build. Operate. Experiment.",
    template: "%s | BusinessLab",
  },
  description:
    "Create virtual businesses, experiment with decisions and discover what drives business performance.",
};

interface RootLayoutProps {
  children: ReactNode;
}

export default function RootLayout({
  children,
}: Readonly<RootLayoutProps>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
