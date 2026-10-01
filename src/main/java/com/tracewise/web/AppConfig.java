package com.tracewise.web;

import com.tracewise.simulator.Simulator;
import com.tracewise.telemetry.Telemetry;
import com.tracewise.topology.Topology;
import com.tracewise.topology.TopologyLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class AppConfig {

    @Bean
    public Topology topology() {
        try (InputStream is = getClass().getResourceAsStream("/topology.json")) {
            if (is != null) {
                return TopologyLoader.load(is);
            }
        } catch (Exception ignored) {
        }
        // Fallback topology if json is empty or missing
        Topology topology = new Topology();
        return topology;
    }

    @Bean
    public Telemetry telemetry(Topology topology, Simulator simulator) {
        Telemetry telemetry = new Telemetry();
        simulator.resetToHealthy(topology, telemetry);
        return telemetry;
    }
}