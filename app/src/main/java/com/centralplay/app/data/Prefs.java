package com.centralplay.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.centralplay.app.BuildConfig;

public final class Prefs {
    private static final String NAME = "central_play";
    private Prefs() {}
    private static SharedPreferences p(Context c) { return c.getSharedPreferences(NAME, Context.MODE_PRIVATE); }

    public static String manifestUrl(Context c) { return p(c).getString("manifest_url", BuildConfig.DEFAULT_MANIFEST_URL); }
    public static void setManifestUrl(Context c, String v) { p(c).edit().putString("manifest_url", v == null ? "" : v.trim()).apply(); }
    public static boolean adultEnabled(Context c) { return p(c).getBoolean("adult_enabled", false); }
    public static void setAdultEnabled(Context c, boolean v) { p(c).edit().putBoolean("adult_enabled", v).apply(); }
    public static String pin(Context c) { return p(c).getString("parental_pin", ""); }
    public static void setPin(Context c, String v) { p(c).edit().putString("parental_pin", v == null ? "" : v.trim()).apply(); }
    public static String syncSource(Context c) { return p(c).getString("sync_source", "Catálogo de demostración"); }
    public static void setSyncState(Context c, String source, String error) { p(c).edit().putString("sync_source", source).putString("sync_error", error == null ? "" : error).putLong("sync_time", System.currentTimeMillis()).apply(); }
    public static long syncTime(Context c) { return p(c).getLong("sync_time", 0); }
    public static String syncError(Context c) { return p(c).getString("sync_error", ""); }
}
