# NEXUS

> everything connected

NEXUS is a Java + Spring Boot observability dashboard for network operations teams. It models a synthetic topology, monitors device health in real time, simulates faults, and highlights outage impact before escalation.

Built for fast operational insight, clear RCA guidance, and clean visual decision support in one place.

## Demo

![NEXUS dashboard overview](docs/demo-nexus-dashboard.png)

### Demo gallery

![NEXUS topology view](docs/nexus-topology-demo.png)

A live preview of the dashboard is available at:
- http://127.0.0.1:5000

## Why NEXUS

- Network health at a glance: device count, healthy/degraded/critical breakdowns, and alert trends
- Topology-aware intelligence: graph-based reachability and dependency analysis
- Fault simulation: realistic network scenarios such as congestion, packet loss, and fiber cuts
- RCA support: identifies critical devices and recommended actions based on role and impact
- Clean operational UI: built with Spring MVC + Thymeleaf and a modern dark observability aesthetic

## Core features

- Full topology visualization with device-level inspection
- Health scoring based on packet loss, latency, and CPU thresholds
- Impact preview for outages and cascading failures
- Scenario-driven simulation engine for operational drills
- CLI and web entry points for analysis and monitoring workflows

## Architecture

NEXUS uses an in-memory model to represent devices, telemetry, health states, and topology links.

- Topology graph: devices and neighbor relationships
- Telemetry engine: synthetic metric generation and device state tracking
- Health evaluation: status thresholds for degraded and critical events
- RCA model: root-cause reasoning based on device criticality and connectivity
- Web layer: dashboard, topology, simulator, and device APIs

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

### 2) Run the app

```bash
mvn spring-boot:run
```

Then open:

```text
http://127.0.0.1:5000
```

### 3) Simulate a fault

Available scenarios:

- `NORMAL`
- `CORE_CONGESTION`
- `EDGE_PACKET_LOSS`
- `FIBER_CUT_SIMULATION`

You can trigger scenarios from the UI, or via the API:

```bash
curl -X POST http://127.0.0.1:5000/api/simulate/CORE_CONGESTION
```

## Project structure

```text
src/main/java/com/tracewise/
  cli/        CLI-driven scenario and analysis flows
  health/     HealthEngine, status calculations
  rca/        Root-cause analysis and action guidance
  simulator/  Fault simulation runner and scenario definitions
  telemetry/  Metric generation and device telemetry state
  topology/   Graph model, topology loader, validation logic
  web/        Controller layer and dashboard APIs

src/main/resources/
  application.properties
  topology.json
  static/style.css
  templates/

src/test/java/
  JUnit tests covering topology, health, RCA, and web flows
```

## Operational logic

- Topology links are modeled as undirected relationships for realistic graph traversal.
- Health thresholds include packet loss, latency, and CPU monitor rules.
- RCA surfaces critical nodes and identifies likely failure propagation paths.
- Synthetic baseline data is pre-seeded for consistent demo behavior and repeatable testing.

## Verification

This project is validated with Maven:

```bash
mvn clean test
```

Current verification status: BUILD SUCCESS with 22 tests passing, 0 failures, 0 errors.

## License

This project is intended for demo, learning, and internal operational visualization use.

## Contact / repo

- GitHub: https://github.com/Malaysao2004/NEXUS
- Project focus: network observability, incident response, and simulation-driven ops intelligence
