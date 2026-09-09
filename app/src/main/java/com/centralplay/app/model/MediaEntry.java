package com.centralplay.app.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MediaEntry {
    public String id = "";
    public String type = "movie"; // live | movie | episode
    public String title = "";
    public String group = "";
    public String description = "";
    public String posterUrl = "";
    public String streamUrl = "";
    public List<String> backupUrls = new ArrayList<>();
    // Optional per-stream headers supplied by an authorized playback resolver/backend.
    public Map<String, String> playbackHeaders = new HashMap<>();
    public boolean adult = false;
    public String year = "";
    public String rating = "";
    public String tvgId = "";
    public String nowTitle = "";
    public String nextTitle = "";

    public List<String> allUrls() {
        List<String> urls = new ArrayList<>();
        if (streamUrl != null && !streamUrl.trim().isEmpty()) urls.add(streamUrl);
        if (backupUrls != null) {
            for (String u : backupUrls) if (u != null && !u.trim().isEmpty() && !urls.contains(u)) urls.add(u);
        }
        return urls;
    }
}
