package com.tracewise;

import com.tracewise.topology.Device;
import com.tracewise.topology.Topology;
import com.tracewise.topology.TopologyLoader;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TopologyTest {
    @Test
    void storesDevicesAndUndirectedLinks() {
        Topology topology = new Topology();
        topology.addDevice(new Device("gw", "Gateway", "CORE"));
        topology.addDevice(new Device("edge", "Edge Switch", "ACCESS"));
        topology.addLink("gw", "edge");

        assertEquals("Gateway", topology.getDevice("gw").getName());
        assertEquals(Set.of("edge"), topology.getNeighbors("gw"));
        assertEquals(Set.of("gw"), topology.getNeighbors("edge"));
        assertTrue(topology.removeDevice("edge"));
        assertTrue(topology.getNeighbors("gw").isEmpty());
    }

    @Test
    void loadsBundledTopologyAndItsLinks() throws Exception {
        try (var input = getClass().getResourceAsStream("/topology.json")) {
            assertNotNull(input);
            Topology topology = TopologyLoader.load(input);
            long linkCount = topology.getAdjacency().values().stream().mapToLong(Set::size).sum() / 2;

            assertEquals(17, topology.getDevices().size());
            assertEquals(20, linkCount);
            assertEquals("internet-gw", topology.getGatewayId());
            assertEquals("CORE", topology.getDevice("core-rtr-01").getRole());
            assertTrue(topology.getNeighbors("internet-gw").contains("core-rtr-01"));
        }
    }

    @Test
    void previewsOutageImpactWithoutIgnoringRedundantPaths() throws Exception {
        try (var input = getClass().getResourceAsStream("/topology.json")) {
            assertNotNull(input);
            Topology topology = TopologyLoader.load(input);

            assertEquals(Set.of("acc-sw-a1", "acc-sw-a2", "srv-app-01", "srv-app-02", "srv-web-01"),
                    topology.getDevicesImpactedByOutage("dist-sw-a"));
            assertTrue(topology.getDevicesImpactedByOutage("core-rtr-01").isEmpty());
        }
    }
}
