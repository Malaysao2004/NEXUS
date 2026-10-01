package com.tracewise.telemetry;

public class DeviceMetrics {
    private double cpuUsage;
    private double memoryUsage;
    private double packetLossRate;
    private double latencyMs;

    public DeviceMetrics() {}
    public DeviceMetrics(double cpuUsage, double memoryUsage, double packetLossRate, double latencyMs) {
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.packetLossRate = packetLossRate;
        this.latencyMs = latencyMs;
    }
    public double getCpuUsage() { return cpuUsage; }
    public void setCpuUsage(double cpu) { this.cpuUsage = cpu; }
    public double getMemoryUsage() { return memoryUsage; }
    public void setMemoryUsage(double mem) { this.memoryUsage = mem; }
    public double getPacketLossRate() { return packetLossRate; }
    public void setPacketLossRate(double loss) { this.packetLossRate = loss; }
    public double getLatencyMs() { return latencyMs; }
    public void setLatencyMs(double lat) { this.latencyMs = lat; }
}