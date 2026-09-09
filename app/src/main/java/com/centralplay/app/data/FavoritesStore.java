package com.centralplay.app.data;

import android.content.Context;
import java.util.HashSet;
import java.util.Set;

public final class FavoritesStore {
    private static final String NAME = "central_play_favorites";
    private FavoritesStore() {}
    private static Set<String> get(Context c) { return new HashSet<>(c.getSharedPreferences(NAME, Context.MODE_PRIVATE).getStringSet("ids", java.util.Collections.emptySet())); }
    public static boolean contains(Context c, String id) { return get(c).contains(id); }
    public static boolean toggle(Context c, String id) {
        Set<String> s = get(c);
        boolean on;
        if (s.contains(id)) { s.remove(id); on=false; } else { s.add(id); on=true; }
        c.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit().putStringSet("ids", s).apply();
        return on;
    }
}
