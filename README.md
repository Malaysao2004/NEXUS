# NEXUS

> everything connected

NEXUS is a Java + Spring Boot network observability dashboard designed for operations teams who need fast visibility into topology health, service impact, and incident-driven decision making.

It combines a synthetic network topology, live-like telemetry simulation, health scoring, and outage impact analysis into a single operational workspace.

## Demo

![NEXUS dashboard overview](docs/demo-nexus-dashboard.png)

![NEXUS topology and impact view](docs/nexus-topology-demo.png)

Local preview:

- http://127.0.0.1:5000

## Why NEXUS

Modern networks are not just a list of devices — they are connected systems where one failure can cascade into multiple outages. NEXUS helps teams answer three questions quickly:

- Is the network healthy right now?
- Which device or dependency is driving the issue?
- What will break next if the problem spreads?

## Core capabilities

- Live-style dashboard for overall network health
- Device and link monitoring using synthetic telemetry
- Topology visualization with dependency-aware graph views
- Outage impact preview before escalation
- Scenario-based fault simulation for training and drills
- Root-cause analysis with operational recommendations
- CLI and web access for both technical and executive workflows

## What makes it useful

- Health scoring based on latency, packet loss, and CPU behavior
- Graph-based dependency awareness across connected devices
- Operational simulation of events like congestion, packet loss, and fiber cuts
- Clear, modern UI built for quick analysis during incidents
- Lightweight architecture that runs fully in memory without external infrastructure

## Architecture

NEXUS follows a compact in-memory model for observability and root-cause analysis.

- Topology layer: device graph and adjacency logic
- Telemetry engine: synthetic metric generation and state tracking
- Health evaluation: device statuses based on thresholds and severity rules
- RCA logic: identifies critical dependencies and likely impact paths
- Web layer: dashboard, topology pages, simulator, and REST endpoints

## Tech stack

- Java 21
- Maven
- Spring Boot 3.3.5
- Thymeleaf
- JUnit 5

## Requirements

- JDK 21
- Maven 3.6+

## Quick start

### 1) Build and test

```bash
mvn clean test
```

### 2) Run the application

```bash
mvn spring-boot:run
```

Then open:

```text
http://127.0.0.1:5000
```

### 3) Simulate a fault

Supported scenarios include:

- `NORMAL`
- `CORE_CONGESTION`
- `EDGE_PACKET_LOSS`
- `FIBER_CUT_SIMULATION`

Trigger from the UI or through the API:

```bash
curl -X POST http://127.0.0.1:5000/api/simulate/CORE_CONGESTION
```

## Project structure

```text
src/main/java/com/tracewise/
  cli/        CLI analysis and operational workflows
  health/     HealthEngine and status evaluation
  rca/        Root-cause analysis and recommended actions
  simulator/  Fault simulation and scenario runners
  telemetry/  Telemetry generation and device metrics
  topology/   Graph model, topology loader, topology logic
  web/        Controllers and dashboard APIs

src/main/resources/
  application.properties
  topology.json
  static/style.css
  templates/

src/test/java/
  JUnit coverage for topology, health, RCA, and web behavior
```

## Operational flow

- Devices are modeled with connectivity and role-based impact behavior.
- Health logic evaluates degraded or critical states based on telemetry thresholds.
- Topology traversal helps identify indirect impact beyond a single failing node.
- RCA highlights likely root causes and actionable next steps for operations teams.

## Verification

The project is verified with Maven using:

```bash
mvn clean test
```

Current result: BUILD SUCCESS with 22 tests passing, 0 failures, and 0 errors.

## GitHub

- Repository: https://github.com/Malaysao2004/NEXUS

## License

This project is intended for demo, learning, and operational visualization use.
