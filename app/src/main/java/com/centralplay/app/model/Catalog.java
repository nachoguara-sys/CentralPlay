package com.centralplay.app.model;

import java.util.ArrayList;
import java.util.List;

public class Catalog {
    public int schema = 1;
    public int version = 1;
    public String updatedAt = "";
    public List<MediaEntry> live = new ArrayList<>();
    public List<MediaEntry> movies = new ArrayList<>();
    public List<SeriesEntry> series = new ArrayList<>();
    public List<EpgEvent> epg = new ArrayList<>();
    public UpdateInfo appUpdate;
}
