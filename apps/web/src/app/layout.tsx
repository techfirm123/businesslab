import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: {
    default: "BusinessLab | Build. Operate. Experiment.",
    template: "%s | BusinessLab",
  },
  description:
    "Create virtual businesses, experiment with decisions and discover what drives business performance.",
};

interface RootLayoutProps {
  children: React.ReactNode;
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
