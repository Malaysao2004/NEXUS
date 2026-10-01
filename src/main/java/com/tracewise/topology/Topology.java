package com.tracewise.topology;

import java.util.*;
import java.util.concurrent.*;

public class Topology {
    private final Map<String, Device> devices = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> adjacency = new ConcurrentHashMap<>();
    private volatile String gatewayId;

    public void addDevice(Device d) {
        if (d != null && d.getId() != null) {
            devices.put(d.getId(), d);
            adjacency.computeIfAbsent(d.getId(), k -> new CopyOnWriteArraySet<>());
        }
    }
    public boolean removeDevice(String id) {
        if (devices.remove(id) != null) {
            adjacency.remove(id);
            adjacency.values().forEach(s -> s.remove(id));
            if (Objects.equals(gatewayId, id)) gatewayId = null;
            return true;
        }
        return false;
    }
    public void addLink(String s, String t) {
        if (devices.containsKey(s) && devices.containsKey(t)) {
            adjacency.computeIfAbsent(s, k -> new CopyOnWriteArraySet<>()).add(t);
            adjacency.computeIfAbsent(t, k -> new CopyOnWriteArraySet<>()).add(s);
        }
    }
    public Collection<Device> getDevices() { return devices.values(); }
    public Device getDevice(String id) { return devices.get(id); }
    public Set<String> getNeighbors(String id) { return adjacency.getOrDefault(id, Collections.emptySet()); }
    public Map<String, Set<String>> getAdjacency() { return adjacency; }
    public String getGatewayId() { return gatewayId; }
    public void setGatewayId(String gatewayId) { this.gatewayId = gatewayId; }

    public Set<String> getDevicesImpactedByOutage(String failedDeviceId) {
        if (!devices.containsKey(failedDeviceId)) {
            throw new NoSuchElementException("Unknown device: " + failedDeviceId);
        }
        if (gatewayId == null || !devices.containsKey(gatewayId)) {
            throw new IllegalStateException("Topology gateway is not configured");
        }

        Set<String> reachable = new HashSet<>();
        if (!gatewayId.equals(failedDeviceId)) {
            Deque<String> pending = new ArrayDeque<>();
            reachable.add(gatewayId);
            pending.add(gatewayId);

            while (!pending.isEmpty()) {
                String current = pending.removeFirst();
                for (String neighbor : getNeighbors(current)) {
                    if (!neighbor.equals(failedDeviceId) && reachable.add(neighbor)) {
                        pending.addLast(neighbor);
                    }
                }
            }
        }

        Set<String> impacted = new TreeSet<>(devices.keySet());
        impacted.remove(failedDeviceId);
        impacted.removeAll(reachable);
        return Collections.unmodifiableSet(impacted);
    }
}