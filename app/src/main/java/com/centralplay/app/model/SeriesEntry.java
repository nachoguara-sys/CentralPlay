package com.centralplay.app.model;

import java.util.ArrayList;
import java.util.List;

public class SeriesEntry {
    public String id = "";
    public String title = "";
    public String description = "";
    public String posterUrl = "";
    public String group = "";
    public boolean adult = false;
    public String year = "";
    public String rating = "";
    public List<Episode> episodes = new ArrayList<>();
}
