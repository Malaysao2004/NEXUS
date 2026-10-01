package com.tracewise.topology;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public final class TopologyLoader {

    private TopologyLoader() {}

    public static Topology load(InputStream inputStream) throws Exception {
        Topology topology = new Topology();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(inputStream);

        if (root.has("devices")) {
            for (JsonNode devNode : root.get("devices")) {
                String id = devNode.get("id").asText();
                String name = devNode.has("name") ? devNode.get("name").asText() : id;
                String tier = devNode.has("tier") ? devNode.get("tier").asText()
                        : devNode.has("role") ? devNode.get("role").asText() : "ACCESS";
                String role = switch (tier.toLowerCase()) {
                    case "core" -> "CORE";
                    case "distribution" -> "AGGREGATION";
                    case "access" -> "ACCESS";
                    default -> tier.toUpperCase();
                };
                topology.addDevice(new Device(id, name, role));
            }
        }

        if (root.hasNonNull("gateway")) {
            String gatewayId = root.get("gateway").asText();
            if (topology.getDevice(gatewayId) == null) {
                throw new IllegalArgumentException("Unknown topology gateway: " + gatewayId);
            }
            topology.setGatewayId(gatewayId);
        }

        if (root.has("links")) {
            for (JsonNode linkNode : root.get("links")) {
                String source = linkNode.has("from") ? linkNode.get("from").asText() : linkNode.get("source").asText();
                String target = linkNode.has("to") ? linkNode.get("to").asText() : linkNode.get("target").asText();
                topology.addLink(source, target);
            }
        }

        return topology;
    }
}