package com.centralplay.app.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import androidx.collection.LruCache;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class ImageLoader {
    private static final LruCache<String, Bitmap> CACHE = new LruCache<>(20);
    private static final ExecutorService EXEC = Executors.newFixedThreadPool(3);
    private static final OkHttpClient HTTP = new OkHttpClient();
    private ImageLoader() {}

    public static void load(ImageView view, String url) {
        view.setImageDrawable(null);
        view.setTag(url);
        if (url == null || url.trim().isEmpty()) return;
        Bitmap cached = CACHE.get(url);
        if (cached != null) { view.setImageBitmap(cached); return; }
        EXEC.execute(() -> {
            try (Response r = HTTP.newCall(new Request.Builder().url(url).build()).execute()) {
                if (!r.isSuccessful() || r.body() == null) return;
                try (InputStream in = r.body().byteStream()) {
                    Bitmap b = BitmapFactory.decodeStream(in);
                    if (b == null) return;
                    CACHE.put(url, b);
                    view.post(() -> { if (url.equals(view.getTag())) view.setImageBitmap(b); });
                }
            } catch (Exception ignored) {}
        });
    }
}
