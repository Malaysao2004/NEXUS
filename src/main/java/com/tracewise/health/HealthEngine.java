package com.tracewise.health;

import com.tracewise.telemetry.*;
import com.tracewise.topology.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class HealthEngine {
    public record Alert(String deviceId, String metric, String message, Status severity) {}
    public record DeviceHealth(Device device, Status status, DeviceMetrics metrics, List<Alert> alerts) {}
    public record HealthReport(Map<String, DeviceHealth> deviceReports, long healthyCount, long degradedCount, long criticalCount) {}

    public HealthReport evaluateHealth(Topology topology, Telemetry telemetry) {
        Map<String, DeviceHealth> reports = new LinkedHashMap<>();
        long[] counts = new long[3]; // 0=healthy, 1=degraded, 2=critical

        for (Device device : topology.getDevices()) {
            DeviceMetrics m = telemetry != null ? telemetry.get(device.getId()) : null;
            if (m == null) m = new DeviceMetrics(10.0, 20.0, 0.0, 2.0);
            
            List<Alert> alerts = new ArrayList<>();
            Status s = Status.HEALTHY;

            if (m.getPacketLossRate() >= 15.0) { alerts.add(new Alert(device.getId(), "Loss", "Severe", Status.CRITICAL)); s = Status.CRITICAL; }
            else if (m.getPacketLossRate() >= 5.0) { alerts.add(new Alert(device.getId(), "Loss", "High", Status.DEGRADED)); s = Status.DEGRADED; }
            
            if (m.getCpuUsage() >= 90.0) { alerts.add(new Alert(device.getId(), "CPU", "Critical", Status.CRITICAL)); s = Status.CRITICAL; }
            else if (m.getCpuUsage() >= 75.0) { alerts.add(new Alert(device.getId(), "CPU", "High", Status.DEGRADED)); if(s != Status.CRITICAL) s = Status.DEGRADED; }

            if (m.getLatencyMs() >= 120.0) { alerts.add(new Alert(device.getId(), "Latency", "Critical", Status.CRITICAL)); s = Status.CRITICAL; }
            else if (m.getLatencyMs() >= 40.0) { alerts.add(new Alert(device.getId(), "Latency", "High", Status.DEGRADED)); if(s != Status.CRITICAL) s = Status.DEGRADED; }

            if (s == Status.CRITICAL) counts[2]++; else if (s == Status.DEGRADED) counts[1]++; else counts[0]++;
            reports.put(device.getId(), new DeviceHealth(device, s, m, alerts));
        }
        return new HealthReport(reports, counts[0], counts[1], counts[2]);
    }
}