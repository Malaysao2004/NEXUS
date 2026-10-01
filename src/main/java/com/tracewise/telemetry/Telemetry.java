package com.tracewise.telemetry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Telemetry {
    private final Map<String, DeviceMetrics> samples = new ConcurrentHashMap<>();

    public void record(String deviceId, DeviceMetrics metrics) { samples.put(deviceId, metrics); }
    public DeviceMetrics get(String deviceId) { return samples.get(deviceId); }
    public void remove(String deviceId) { samples.remove(deviceId); }
}