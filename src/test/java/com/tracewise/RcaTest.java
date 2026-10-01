package com.tracewise;

import com.tracewise.health.HealthEngine;
import com.tracewise.health.HealthEngine.HealthReport;
import com.tracewise.rca.RcaEngine;
import com.tracewise.rca.RcaEngine.RcaResult;
import com.tracewise.rca.RcaEngine.RootCause;
import com.tracewise.simulator.Simulator;
import com.tracewise.telemetry.Telemetry;
import com.tracewise.topology.Device;
import com.tracewise.topology.Topology;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RcaTest {
    private Topology topo;
    private final Simulator sim = new Simulator();
    private final HealthEngine healthEngine = new HealthEngine();
    private final RcaEngine rcaEngine = new RcaEngine();
    private Telemetry telemetry;

    @BeforeEach
    void setUp() {
        topo = new Topology();
        // Setup a small mock topology for testing RCA logic
        topo.addDevice(new Device("core-1", "Core Switch", "CORE"));
        topo.addDevice(new Device("agg-1", "Agg Switch", "AGGREGATION"));
        topo.addDevice(new Device("acc-1", "Access Switch", "ACCESS"));

        topo.addLink("core-1", "agg-1");
        topo.addLink("agg-1", "acc-1");

        telemetry = new Telemetry();
    }

    private RcaResult analyze(String scenario) {
        sim.runScenario(scenario, topo, telemetry);
        HealthReport report = healthEngine.evaluateHealth(topo, telemetry);
        return rcaEngine.analyze(topo, report);
    }

    @Test
    void healthyHasNoRootCauses() {
        RcaResult r = analyze("NORMAL");
        
        assertTrue(r.probableCauses().isEmpty(), "Healthy network should have no root causes");
        assertTrue(r.summary().contains("No network anomalies") || r.summary().contains("baseline"));
    }

    @Test
    void coreCongestionYieldsCoreRootCause() {
        RcaResult r = analyze("CORE_CONGESTION");
        List<String> rootDeviceIds = r.probableCauses().stream().map(RootCause::deviceId).toList();

        assertTrue(rootDeviceIds.contains("core-1"), "Core switch must be identified in core congestion scenario");
        
        RootCause rc = r.probableCauses().stream().filter(c -> c.deviceId().equals("core-1")).findFirst().get();
        assertTrue(rc.reason().contains("CRITICAL"));
        assertTrue(rc.recommendedAction().contains("Reroute"));
    }

    @Test
    void fiberCutYieldsAggregationRootCause() {
        RcaResult r = analyze("FIBER_CUT_SIMULATION");
        List<String> rootDeviceIds = r.probableCauses().stream().map(RootCause::deviceId).toList();

        assertTrue(rootDeviceIds.contains("agg-1"), "Aggregation switch must be identified during fiber cut");
        
        RootCause rc = r.probableCauses().stream().filter(c -> c.deviceId().equals("agg-1")).findFirst().get();
        assertTrue(rc.recommendedAction().contains("upstream trunk"));
    }

    @Test
    void edgeLossYieldsAccessRootCause() {
        RcaResult r = analyze("EDGE_PACKET_LOSS");
        List<String> rootDeviceIds = r.probableCauses().stream().map(RootCause::deviceId).toList();

        assertTrue(rootDeviceIds.contains("acc-1"), "Access switch must be identified during edge drop");
        
        RootCause rc = r.probableCauses().stream().filter(c -> c.deviceId().equals("acc-1")).findFirst().get();
        assertTrue(rc.recommendedAction().contains("Reboot access switch"));
        assertTrue(rc.confidenceScore() >= 0.75);
    }
}