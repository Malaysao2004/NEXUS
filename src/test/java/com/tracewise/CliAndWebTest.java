package com.tracewise;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
class CliAndWebTest {
    @Autowired MockMvc mvc;

    @Test
    void dashboardLoads() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("NEXUS")))
                .andExpect(content().string(containsString("everything connected")));
    }

    @Test
    void devicesPageLoads() throws Exception {
        mvc.perform(get("/devices"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Device Inventory")));
    }

    @Test
    void simulatorPageLoads() throws Exception {
        mvc.perform(get("/simulator").param("scenario", "CORE_CONGESTION"))
                .andExpect(status().isOk());
    }

    @Test
    void apiRunsKnownScenario() throws Exception {
        mvc.perform(post("/api/simulate/NORMAL"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Scenario applied: NORMAL")));
    }

    @Test
    void apiRejectsUnknownScenario() throws Exception {
        mvc.perform(post("/api/simulate/unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Unknown scenario: unknown")));
    }

    @Test
    void apiReturnsBundledDevices() throws Exception {
        mvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("core-rtr-01")));
    }

    @Test
    void impactPreviewReportsIsolatedDevices() throws Exception {
        mvc.perform(get("/api/impact-preview/dist-sw-a"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.failedDeviceId").value("dist-sw-a"))
                .andExpect(jsonPath("$.impactedCount").value(5))
                .andExpect(content().string(containsString("srv-web-01")));
    }

    @Test
    void impactPreviewRejectsUnknownDevice() throws Exception {
        mvc.perform(get("/api/impact-preview/not-a-device"))
                .andExpect(status().isNotFound());
    }

    @Test
    void dashboardLinksRootCausesToImpactPreview() throws Exception {
        mvc.perform(post("/api/simulate/CORE_CONGESTION"))
                .andExpect(status().isOk());

        mvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Preview impact")))
                .andExpect(content().string(containsString("/topology?device=core-rtr-01")));
    }
}
