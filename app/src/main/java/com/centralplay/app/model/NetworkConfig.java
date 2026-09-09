package com.centralplay.app.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Runtime network configuration loaded from the Central Play manifest. */
public class NetworkConfig {
    public List<String> apiNodes = new ArrayList<>();
    public List<String> webSocketNodes = new ArrayList<>();
    public String healthPath = "/health";
    public String playlistPath = "/api/v1/content/playlist";
    public String userAgent = "CentralPlay/1.0";
    public int connectTimeoutSeconds = 10;
    public int readTimeoutSeconds = 20;
    public int pingIntervalSeconds = 30;
    public int baseReconnectSeconds = 3;
    public int maxReconnectSeconds = 60;
    public int nodeCooldownSeconds = 30;
    public Map<String, String> publicHeaders = new LinkedHashMap<>();
}
