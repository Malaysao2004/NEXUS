package com.tracewise;

import com.tracewise.health.HealthEngine;
import com.tracewise.health.HealthEngine.HealthReport;
import com.tracewise.health.Status;
import com.tracewise.telemetry.DeviceMetrics;
import com.tracewise.telemetry.Telemetry;
import com.tracewise.topology.Device;
import com.tracewise.topology.Topology;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HealthTest {
    private Topology topo;
    private final HealthEngine engine = new HealthEngine();

    @BeforeEach
    void setUp() {
        topo = new Topology();
        topo.addDevice(new Device("gw", "GW", "CORE"));
        topo.addDevice(new Device("n1", "Node 1", "ACCESS"));
        topo.addLink("gw", "n1");
    }

    private HealthReport reportFor(DeviceMetrics n1Metrics) {
        Telemetry tel = new Telemetry();
        tel.record("gw", new DeviceMetrics(10.0, 20.0, 0.0, 5.0)); // Default healthy gateway
        tel.record("n1", n1Metrics);
        return engine.evaluateHealth(topo, tel);
    }

    @Test
    void healthyBaseline() {
        HealthReport r = reportFor(new DeviceMetrics(15.0, 30.0, 0.0, 2.0));
        
        assertEquals(Status.HEALTHY, r.deviceReports().get("n1").status());
        assertEquals(2, r.healthyCount());
        assertEquals(0, r.criticalCount());
    }

    @Test
    void criticalFromHighLoss() {
        // 25% Packet Loss
        HealthReport r = reportFor(new DeviceMetrics(15.0, 30.0, 25.0, 2.0));
        
        assertEquals(Status.CRITICAL, r.deviceReports().get("n1").status());
        assertEquals(1, r.criticalCount());
    }

    @Test
    void degradedFromLatency() {
        // 60ms latency
        HealthReport r = reportFor(new DeviceMetrics(15.0, 30.0, 0.0, 60.0));
        
        assertEquals(Status.DEGRADED, r.deviceReports().get("n1").status());
        assertTrue(r.deviceReports().get("n1").alerts().stream().anyMatch(a -> a.metric().equals("Latency")));
    }

    @Test
    void criticalFromCpu() {
        // 95% CPU
        HealthReport r = reportFor(new DeviceMetrics(95.0, 30.0, 0.0, 2.0));
        
        assertEquals(Status.CRITICAL, r.deviceReports().get("n1").status());
    }

    @Test
    void multipleBreachesProduceMultipleAlerts() {
        // 95% CPU (Critical) and 10% Loss (Degraded)
        HealthReport r = reportFor(new DeviceMetrics(95.0, 30.0, 10.0, 2.0));
        
        assertEquals(Status.CRITICAL, r.deviceReports().get("n1").status());
        assertEquals(2, r.deviceReports().get("n1").alerts().size());
    }

    @Test
    void missingTelemetryTreatedAsHealthyDefaults() {
        Telemetry tel = new Telemetry();
        tel.record("gw", new DeviceMetrics(15.0, 30.0, 0.0, 2.0));
        
        // 'n1' is missing telemetry, evaluateHealth should apply default idle values
        HealthReport r = engine.evaluateHealth(topo, tel);
        
        assertEquals(Status.HEALTHY, r.deviceReports().get("n1").status());
    }
}