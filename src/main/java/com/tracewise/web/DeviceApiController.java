package com.tracewise.web;

import com.tracewise.health.HealthEngine;
import com.tracewise.simulator.Simulator;
import com.tracewise.telemetry.DeviceMetrics;
import com.tracewise.telemetry.Telemetry;
import com.tracewise.topology.Device;
import com.tracewise.topology.Topology;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class DeviceApiController {

    private final Topology topology;
    private final Telemetry telemetry;
    private final HealthEngine healthEngine;
    private final Simulator simulator;

    public DeviceApiController(Topology topology, Telemetry telemetry, HealthEngine healthEngine, Simulator simulator) {
        this.topology = topology;
        this.telemetry = telemetry;
        this.healthEngine = healthEngine;
        this.simulator = simulator;
    }

    public record DevicePayload(String id, String name, String role, List<String> neighbors) {}
    public record GraphNode(String id, String label, String group, String status) {}
    public record GraphEdge(String from, String to) {}
    public record GraphData(List<GraphNode> nodes, List<GraphEdge> edges) {}
    public record ImpactedDevice(String id, String name, String role, String status) {}
    public record ImpactPreview(String failedDeviceId, String failedDeviceName, int impactedCount,
                                List<ImpactedDevice> impactedDevices) {}

    @GetMapping("/devices")
    public Collection<Device> getDevices() {
        return topology.getDevices();
    }

    @PostMapping("/devices")
    public ResponseEntity<Device> addOrUpdateDevice(@RequestBody DevicePayload payload) {
        if (payload.id() == null || payload.id().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Device device = new Device(payload.id().trim(), payload.name().trim(), payload.role());
        topology.addDevice(device);

        if (payload.neighbors() != null) {
            for (String neighborId : payload.neighbors()) {
                topology.addLink(device.getId(), neighborId.trim());
            }
        }

        // Initialize baseline telemetry for this newly added device
        if (telemetry.get(device.getId()) == null) {
            telemetry.record(device.getId(), new DeviceMetrics(12.0, 25.0, 0.0, 2.5));
        }

        return ResponseEntity.ok(device);
    }

    @DeleteMapping("/devices/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable String id) {
        boolean removed = topology.removeDevice(id);
        if (removed) {
            telemetry.remove(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/devices/{id}/metrics")
    public ResponseEntity<Void> updateMetrics(@PathVariable String id, @RequestBody DeviceMetrics metrics) {
        if (topology.getDevice(id) == null) {
            return ResponseEntity.notFound().build();
        }
        telemetry.record(id, metrics);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/graph-data")
    public ResponseEntity<GraphData> getGraphData() {
        HealthEngine.HealthReport report = healthEngine.evaluateHealth(topology, telemetry);
        List<GraphNode> nodes = new ArrayList<>();
        List<GraphEdge> edges = new ArrayList<>();
        Set<String> processedEdges = new HashSet<>();

        for (Device d : topology.getDevices()) {
            HealthEngine.DeviceHealth health = report.deviceReports().get(d.getId());
            String status = health != null ? health.status().name() : "HEALTHY";
            nodes.add(new GraphNode(d.getId(), d.getName() + "\n(" + d.getRole() + ")", d.getRole(), status));

            for (String neighbor : topology.getNeighbors(d.getId())) {
                String edgeKey = d.getId().compareTo(neighbor) < 0 ? d.getId() + "->" + neighbor : neighbor + "->" + d.getId();
                if (processedEdges.add(edgeKey)) {
                    edges.add(new GraphEdge(d.getId(), neighbor));
                }
            }
        }

        return ResponseEntity.ok(new GraphData(nodes, edges));
    }

    @GetMapping("/impact-preview/{deviceId}")
    public ResponseEntity<ImpactPreview> previewImpact(@PathVariable String deviceId) {
        Device failedDevice = topology.getDevice(deviceId);
        if (failedDevice == null) {
            return ResponseEntity.notFound().build();
        }
        if (topology.getGatewayId() == null) {
            return ResponseEntity.unprocessableEntity().build();
        }

        HealthEngine.HealthReport report = healthEngine.evaluateHealth(topology, telemetry);
        List<ImpactedDevice> impactedDevices = topology.getDevicesImpactedByOutage(deviceId).stream()
                .map(id -> {
                    Device device = topology.getDevice(id);
                    HealthEngine.DeviceHealth health = report.deviceReports().get(id);
                    return new ImpactedDevice(id, device.getName(), device.getRole(), health.status().name());
                })
                .toList();

        return ResponseEntity.ok(new ImpactPreview(deviceId, failedDevice.getName(), impactedDevices.size(), impactedDevices));
    }

    @PostMapping("/simulate/{scenario}")
    public ResponseEntity<String> triggerSimulation(@PathVariable String scenario) {
        String normalizedScenario = scenario.toUpperCase(Locale.ROOT);
        if (!Simulator.SCENARIOS.contains(normalizedScenario)) {
            return ResponseEntity.badRequest().body("Unknown scenario: " + scenario);
        }
        simulator.runScenario(normalizedScenario, topology, telemetry);
        return ResponseEntity.ok("Scenario applied: " + normalizedScenario);
    }
}