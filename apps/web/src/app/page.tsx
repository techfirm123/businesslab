import Link from "next/link";

const capabilities = [
  {
    number: "01",
    title: "Build your business",
    description:
      "Explore different business models, configure your business and create something of your own.",
    label: "Business creation",
  },
  {
    number: "02",
    title: "See your business evolve",
    description:
      "Operate in a persistent virtual environment where customers, resources, events and decisions shape what happens next.",
    label: "Business simulation",
  },
  {
    number: "03",
    title: "Design how it operates",
    description:
      "Create visual workflows and operating policies to shape how your business responds to everyday situations.",
    label: "Workflow studio",
  },
  {
    number: "04",
    title: "Experiment with decisions",
    description:
      "Explore alternative strategies, run controlled simulations and compare business outcomes.",
    label: "Experimentation",
  },
  {
    number: "05",
    title: "Understand what happens",
    description:
      "Explore financial and operational results, discover why they changed and learn from every decision.",
    label: "Business intelligence",
  },
  {
    number: "06",
    title: "Get an intelligent advisor",
    description:
      "Work with specialised AI business agents that help analyse problems, explore possibilities and improve decisions.",
    label: "AI intelligence",
  },
];

const principles = [
  {
    number: "01",
    title: "A world of businesses",
    description:
      "One platform, multiple business models and a shared simulation foundation.",
  },
  {
    number: "02",
    title: "Real rules. Virtual risks.",
    description:
      "Deterministic business mechanics make outcomes consistent, reproducible and explainable.",
  },
  {
    number: "03",
    title: "Your business keeps going",
    description:
      "Persistent business lifecycles let you return and discover what happened while you were away.",
  },
];

export default function Home() {
  return (
    <main className="site-shell">
      <header className="topbar">
        <Link className="brand" href="/" aria-label="BusinessLab home">
          <span className="brand-symbol">B</span>
          <span className="brand-name">
            Business<span>Lab</span>
          </span>
        </Link>

        <nav className="top-navigation" aria-label="Main navigation">
          <a href="#platform">The platform</a>
          <a href="#principles">Our approach</a>
        </nav>

        <div className="platform-status">
          <span className="status-dot" />
          <span>Building the foundation</span>
        </div>
      </header>

      <section className="hero-section">
        <div className="hero-copy">
          <div className="eyebrow">
            <span className="eyebrow-line" />
            A VIRTUAL WORLD FOR BUSINESS EXPERIMENTATION
          </div>

          <h1>
            Build a business.
            <br />
            <span>Discover what makes it work.</span>
          </h1>

          <p className="hero-description">
            What if you could start a business, make decisions, experience
            the consequences and try again — without risking real money?
          </p>

          <p className="hero-supporting">
            Welcome to a world where business ideas become experiences
            and decisions become experiments.
          </p>

          <div className="hero-actions">
            <a className="primary-link" href="#platform">
              Explore BusinessLab
              <span aria-hidden="true">↗</span>
            </a>

            <span className="hero-note">
              Start with an idea. Learn by doing.
            </span>
          </div>
        </div>

        <div
          className="hero-visual"
          role="img"
          aria-label="Illustration of connected virtual businesses and simulation workflows"
        >
          <div className="visual-header">
            <span>BUSINESSLAB / WORLD 001</span>
            <span className="visual-live">
              <span className="status-dot" />
              VIRTUAL ENVIRONMENT
            </span>
          </div>

          <div className="visual-world">
            <div className="world-label world-label-one">
              <span className="world-icon">01</span>
              <span>CREATE</span>
            </div>

            <div className="world-label world-label-two">
              <span className="world-icon">02</span>
              <span>OPERATE</span>
            </div>

            <div className="world-label world-label-three">
              <span className="world-icon">03</span>
              <span>EXPERIMENT</span>
            </div>

            <div className="world-center">
              <span className="center-orbit orbit-one" />
              <span className="center-orbit orbit-two" />
              <div className="center-symbol">
                <span>B</span>
              </div>
              <span className="center-caption">BUSINESSLAB</span>
            </div>

            <div className="world-connector connector-one" />
            <div className="world-connector connector-two" />
            <div className="world-connector connector-three" />
          </div>

          <div className="visual-footer">
            <div>
              <span className="footer-label">PLATFORM FOUNDATION</span>
              <strong>One world. Many possibilities.</strong>
            </div>
            <span className="footer-coordinate">BL — 001</span>
          </div>
        </div>
      </section>

      <section className="platform-section" id="platform">
        <div className="section-heading">
          <div>
            <div className="eyebrow">
              <span className="eyebrow-line" />
              THE BUSINESSLAB EXPERIENCE
            </div>

            <h2>
              More than a simulation.
              <br />
              <span>A place to experiment.</span>
            </h2>
          </div>

          <p>
            Everything you need to create, operate and understand
            virtual businesses — brought together in one evolving platform.
          </p>
        </div>

        <div className="capability-grid">
          {capabilities.map((capability) => (
            <article
              className="capability-card"
              key={capability.number}
            >
              <div className="card-top">
                <span className="card-number">{capability.number}</span>
                <span className="card-indicator" aria-hidden="true">
                  ↗
                </span>
              </div>

              <div className="card-content">
                <span className="card-label">{capability.label}</span>
                <h3>{capability.title}</h3>
                <p>{capability.description}</p>
              </div>
            </article>
          ))}
        </div>
      </section>

      <section className="principles-section" id="principles">
        <div className="principles-intro">
          <div className="eyebrow">
            <span className="eyebrow-line" />
            BUILT ON THREE PRINCIPLES
          </div>

          <h2>
            Simple to start.
            <br />
            <span>Powerful to explore.</span>
          </h2>

          <p>
            BusinessLab should be accessible to someone exploring their
            first business idea and powerful enough for someone who wants
            to investigate complex business decisions.
          </p>
        </div>

        <div className="principles-list">
          {principles.map((principle) => (
            <article className="principle-item" key={principle.number}>
              <span className="principle-number">
                {principle.number}
              </span>

              <div>
                <h3>{principle.title}</h3>
                <p>{principle.description}</p>
              </div>
            </article>
          ))}
        </div>
      </section>

      <section className="launch-section">
        <span className="launch-overline">
          THE JOURNEY BEGINS WITH ONE BUSINESS
        </span>

        <h2>
          A world of businesses.
          <br />
          <span>One experiment at a time.</span>
        </h2>

        <p>
          We&apos;re building the foundations for a new way to explore
          entrepreneurship, business operations and decision-making.
        </p>

        <span className="launch-status">
          <span className="status-dot" />
          BusinessLab is under development
        </span>
      </section>

      <footer className="footer">
        <Link className="brand footer-brand" href="/">
          <span className="brand-symbol">B</span>
          <span className="brand-name">
            Business<span>Lab</span>
          </span>
        </Link>

        <p>Build. Operate. Experiment. Understand.</p>

        <span>© {new Date().getFullYear()} BusinessLab</span>
      </footer>
    </main>
  );
}
