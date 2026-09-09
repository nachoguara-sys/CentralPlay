package com.centralplay.app.model;

import java.util.ArrayList;
import java.util.List;

public class Episode {
    public String id = "";
    public int season = 1;
    public int number = 1;
    public String title = "";
    public String description = "";
    public String streamUrl = "";
    public List<String> backupUrls = new ArrayList<>();
}
