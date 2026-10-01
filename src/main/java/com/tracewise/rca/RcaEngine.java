package com.tracewise.rca;

import com.tracewise.health.HealthEngine;
import com.tracewise.health.Status;
import com.tracewise.topology.Device;
import com.tracewise.topology.Topology;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RcaEngine {

    public record RootCause(String deviceId, String reason, String recommendedAction, double confidenceScore) {}

    public record RcaResult(List<RootCause> probableCauses, String summary) {}

    public RcaResult analyze(Topology topology, HealthEngine.HealthReport report) {
        List<RootCause> causes = new ArrayList<>();

        for (Map.Entry<String, HealthEngine.DeviceHealth> entry : report.deviceReports().entrySet()) {
            String deviceId = entry.getKey();
            HealthEngine.DeviceHealth health = entry.getValue();

            if (health.status() == Status.CRITICAL) {
                Device dev = health.device();
                Set<String> neighbors = topology.getNeighbors(deviceId);

                long affectedNeighbors = neighbors.stream()
                        .map(n -> report.deviceReports().get(n))
                        .filter(Objects::nonNull)
                        .filter(h -> h.status() != Status.HEALTHY)
                        .count();

                double confidence = 0.75 + (affectedNeighbors > 0 ? 0.20 : 0.0);
                String action = switch (dev.getRole()) {
                    case "CORE" -> "Reroute BGP/OSPF traffic to redundant Core switch and inspect fiber transceiver.";
                    case "AGGREGATION" -> "Verify upstream trunk link saturation and check spanning-tree topology.";
                    default -> "Reboot access switch or inspect local drop cable/port.";
                };

                causes.add(new RootCause(
                        deviceId,
                        "Device " + dev.getName() + " (" + dev.getRole() + ") is CRITICAL with " + health.alerts().size() + " active alerts impacting " + affectedNeighbors + " neighbor links.",
                        action,
                        Math.min(confidence, 0.99)
                ));
            }
        }

        String summary = causes.isEmpty()
                ? "No network anomalies or critical root causes detected. All devices operating within baseline."
                : "Identified " + causes.size() + " probable root cause(s) degrading network path performance.";

        return new RcaResult(causes, summary);
    }
}