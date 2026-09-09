package com.centralplay.app.model;

import java.util.ArrayList;
import java.util.List;

public class CentralManifest {
    public int schema = 1;
    public String catalog = "";
    public String liveM3u = "";
    public String epgXmltv = "";
    public String moviesJson = "";
    public String seriesJson = "";
    public List<String> catalogBackups = new ArrayList<>();
    public List<String> liveBackups = new ArrayList<>();
    public List<String> epgBackups = new ArrayList<>();
    public NetworkConfig network = new NetworkConfig();
    public UpdateInfo appUpdate;
}
