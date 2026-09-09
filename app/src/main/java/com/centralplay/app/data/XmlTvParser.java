package com.centralplay.app.data;

import android.util.Xml;
import com.centralplay.app.model.EpgEvent;
import com.centralplay.app.model.MediaEntry;
import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Streaming XMLTV parser. It never loads the whole XML document in memory.
 */
public final class XmlTvParser {
    private XmlTvParser() {}

    public static List<EpgEvent> parse(InputStream inputStream) {
        List<EpgEvent> out = new ArrayList<>();
        if (inputStream == null) return out;

        try {
            XmlPullParser p = Xml.newPullParser();
            p.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            p.setInput(inputStream, "UTF-8");

            EpgEvent current = null;
            int event = p.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    String tag = p.getName();
                    if ("programme".equals(tag)) {
                        current = new EpgEvent();
                        current.channelId = safe(p.getAttributeValue(null, "channel"));
                        current.startMs = parseTime(p.getAttributeValue(null, "start"));
                        current.stopMs = parseTime(p.getAttributeValue(null, "stop"));
                    } else if (current != null && "title".equals(tag)) {
                        current.title = safe(p.nextText()).trim();
                    } else if (current != null && "desc".equals(tag)) {
                        current.description = safe(p.nextText()).trim();
                    }
                } else if (event == XmlPullParser.END_TAG
                        && "programme".equals(p.getName())
                        && current != null) {
                    if (!current.channelId.isEmpty() && current.startMs > 0L) out.add(current);
                    current = null;
                }
                event = p.next();
            }
        } catch (Exception ignored) {}
        return out;
    }

    public static List<EpgEvent> parse(String xml) {
        if (xml == null || xml.trim().isEmpty()) return new ArrayList<>();
        return parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    public static void applyNowNext(List<MediaEntry> live, List<EpgEvent> events) {
        long now = System.currentTimeMillis();
        Map<String, List<EpgEvent>> byChannel = new HashMap<>();
        for (EpgEvent e : events) {
            if (e == null || e.channelId == null || e.channelId.isEmpty()) continue;
            byChannel.computeIfAbsent(e.channelId, key -> new ArrayList<>()).add(e);
        }
        for (List<EpgEvent> list : byChannel.values()) list.sort(Comparator.comparingLong(a -> a.startMs));
        for (MediaEntry channel : live) {
            if (channel == null || channel.tvgId == null || channel.tvgId.trim().isEmpty()) continue;
            List<EpgEvent> list = byChannel.get(channel.tvgId);
            if (list == null) continue;
            EpgEvent current = null, next = null;
            for (EpgEvent event : list) {
                if (event.startMs <= now && (event.stopMs == 0 || now < event.stopMs)) current = event;
                if (event.startMs > now) { next = event; break; }
            }
            if (current != null) channel.nowTitle = current.title;
            if (next != null) channel.nextTitle = next.title;
        }
    }

    private static long parseTime(String raw) {
        if (raw == null) return 0L;
        String value = raw.trim();
        if (value.isEmpty()) return 0L;
        String[] patterns = {"yyyyMMddHHmmss Z","yyyyMMddHHmmssZ","yyyyMMddHHmm Z","yyyyMMddHHmmZ","yyyyMMddHHmmss","yyyyMMddHHmm"};
        for (String pattern : patterns) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
                format.setLenient(false);
                if (!pattern.contains("Z")) format.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = format.parse(value);
                if (date != null) return date.getTime();
            } catch (Exception ignored) {}
        }
        return 0L;
    }
    private static String safe(String value) { return value == null ? "" : value; }
}
