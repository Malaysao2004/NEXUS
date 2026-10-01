package com.tracewise.web;

import com.tracewise.health.HealthEngine;
import com.tracewise.rca.RcaEngine;
import com.tracewise.simulator.Simulator;
import com.tracewise.telemetry.Telemetry;
import com.tracewise.topology.Topology;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DashboardController {

    private final Topology topology;
    private final Telemetry telemetry;
    private final HealthEngine healthEngine;
    private final RcaEngine rcaEngine;
    private final Simulator simulator;

    public DashboardController(Topology topology, Telemetry telemetry, HealthEngine healthEngine, RcaEngine rcaEngine, Simulator simulator) {
        this.topology = topology;
        this.telemetry = telemetry;
        this.healthEngine = healthEngine;
        this.rcaEngine = rcaEngine;
        this.simulator = simulator;
    }

    private void attachCommon(Model model, String page) {
        model.addAttribute("activePage", page);
        model.addAttribute("report", healthEngine.evaluateHealth(topology, telemetry));
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        attachCommon(model, "dashboard");
        model.addAttribute("rca", rcaEngine.analyze(topology, healthEngine.evaluateHealth(topology, telemetry)));
        model.addAttribute("deviceCount", topology.getDevices().size());
        return "dashboard";
    }

    @GetMapping("/topology")
    public String topologyView(Model model, @RequestParam(name = "device", required = false) String selectedDevice) {
        attachCommon(model, "topology");
        model.addAttribute("devices", topology.getDevices());
        model.addAttribute("adjacency", topology.getAdjacency());
        model.addAttribute("selectedDevice", selectedDevice);
        return "topology";
    }

    @GetMapping("/devices")
    public String deviceManagement(Model model) {
        attachCommon(model, "devices");
        model.addAttribute("devices", topology.getDevices());
        return "devices";
    }

    @GetMapping("/rca")
    public String rcaView(Model model) {
        attachCommon(model, "rca");
        model.addAttribute("rca", rcaEngine.analyze(topology, healthEngine.evaluateHealth(topology, telemetry)));
        return "rca";
    }

    @GetMapping("/simulator")
    public String simulatorView(Model model, @RequestParam(name = "scenario", required = false) String scenarioName) {
        if (scenarioName != null && Simulator.SCENARIOS.contains(scenarioName.toUpperCase())) {
            simulator.runScenario(scenarioName.toUpperCase(), topology, telemetry);
        }
        attachCommon(model, "simulator");
        model.addAttribute("scenarios", Simulator.SCENARIOS);
        return "simulator";
    }
}