package com.centralplay.app.data;

import android.content.Context;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class FavoritesStore {
    private static final String NAME = "central_play_favorites";
    private final Context context;

    public FavoritesStore(Context context) {
        this.context = context.getApplicationContext();
    }

    private static Set<String> get(Context c) {
        return new HashSet<>(c.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .getStringSet("ids", Collections.emptySet()));
    }

    public static boolean contains(Context c, String id) {
        return id != null && get(c).contains(id);
    }

    public static boolean toggle(Context c, String id) {
        if (id == null || id.trim().isEmpty()) return false;
        Set<String> favorites = get(c);
        boolean nowFavorite;
        if (favorites.contains(id)) {
            favorites.remove(id);
            nowFavorite = false;
        } else {
            favorites.add(id);
            nowFavorite = true;
        }
        c.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit().putStringSet("ids", favorites).apply();
        return nowFavorite;
    }

    public boolean isFavorite(String id) { return contains(context, id); }
    public boolean toggleFavorite(String id) { return toggle(context, id); }
    public Set<String> getFavoriteIds() { return get(context); }
}
