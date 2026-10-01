# NEXUS — everything connected

Network observability, outage impact previews, and reachability-based root cause analysis.
Built with Java 21, Maven, and Spring Boot 3.

## Requirements
- JDK 21
- Maven 3.6+

## Build and test
```
mvn clean test
mvn package
```

## Run
Web dashboard (http://127.0.0.1:5000):
```
mvn spring-boot:run
```
Choose a scenario on the Simulator page. The API also supports `POST /api/simulate/{scenario}`.

Scenarios: `NORMAL`, `CORE_CONGESTION`, `EDGE_PACKET_LOSS`, `FIBER_CUT_SIMULATION`.

## How it works
- Topology links are stored as undirected neighbor relationships.
- Health alerts use packet-loss thresholds of 5/15 %, latency thresholds of 40/120 ms, and
  CPU thresholds of 75/90 % for degraded/critical status.
- RCA reports critical devices and recommends actions based on their network role.

## Layout
```
src/main/java/com/tracewise/
  topology/   Topology graph, Device, TopologyLoader (JSON + validation)
  telemetry/  DeviceMetrics, Telemetry (seeded baseline)
  health/     HealthEngine, Status
  rca/        RcaEngine
  simulator/  Simulator + scenarios
  cli/        Cli (scenarios, analyze)
  web/        DashboardController, AppConfig
src/main/resources/  topology.json, application.properties, templates/dashboard.html, static/style.css
src/test/java/       JUnit 5 tests
```

## Notes
- The synthetic baseline uses `java.util.Random(7)`; exact baseline numbers differ from the
  Python/NumPy version, but statuses, RCA results and thresholds are the same.
