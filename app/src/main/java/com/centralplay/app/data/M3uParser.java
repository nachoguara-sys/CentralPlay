package com.centralplay.app.data;

import com.centralplay.app.model.MediaEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class M3uParser {
    private static final Pattern ATTR = Pattern.compile("([\\w-]+)=\\\"([^\\\"]*)\\\"");
    private M3uParser() {}

    public static List<MediaEntry> parse(String text) {
        List<MediaEntry> out = new ArrayList<>();
        if (text == null) return out;
        String[] lines = text.replace("\\r", "").split("\\n");
        MediaEntry pending = null;
        int auto = 1;
        for (String raw : lines) {
            String line = raw.trim();
            if (line.startsWith("#EXTINF:")) {
                pending = new MediaEntry();
                pending.type = "live";
                pending.id = "live_" + auto++;
                Matcher m = ATTR.matcher(line);
                while (m.find()) {
                    String k=m.group(1), v=m.group(2);
                    if ("tvg-id".equals(k)) { pending.tvgId=v; if (!v.trim().isEmpty()) pending.id=v; }
                    else if ("tvg-logo".equals(k)) pending.posterUrl=v;
                    else if ("group-title".equals(k)) pending.group=v;
                }
                int comma = line.lastIndexOf(',');
                pending.title = comma >= 0 ? line.substring(comma+1).trim() : pending.id;
            } else if (!line.isEmpty() && !line.startsWith("#") && pending != null) {
                pending.streamUrl = line;
                out.add(pending);
                pending = null;
            }
        }
        return out;
    }
}
