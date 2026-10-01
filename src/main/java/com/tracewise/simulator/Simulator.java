package com.tracewise.simulator;

import com.tracewise.telemetry.*;
import com.tracewise.topology.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class Simulator {
    public static final List<String> SCENARIOS = List.of("NORMAL", "CORE_CONGESTION", "EDGE_PACKET_LOSS", "FIBER_CUT_SIMULATION");

    public void runScenario(String scenario, Topology topology, Telemetry telemetry) {
        for (Device d : topology.getDevices()) {
            double cpu = 15.0, mem = 30.0, loss = 0.0, lat = 2.0;
            if ("CORE_CONGESTION".equals(scenario) && "CORE".equalsIgnoreCase(d.getRole())) { cpu = 95.0; lat = 150.0; loss = 5.0; }
            if ("EDGE_PACKET_LOSS".equals(scenario) && "ACCESS".equalsIgnoreCase(d.getRole())) { loss = 25.0; lat = 80.0; }
            if ("FIBER_CUT_SIMULATION".equals(scenario) && "AGGREGATION".equalsIgnoreCase(d.getRole())) { loss = 100.0; lat = 500.0; }
            telemetry.record(d.getId(), new DeviceMetrics(cpu, mem, loss, lat));
        }
    }

    public void resetToHealthy(Topology topology, Telemetry telemetry) {
        runScenario("NORMAL", topology, telemetry);
    }
}