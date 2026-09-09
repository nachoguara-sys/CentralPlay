package com.centralplay.app.data;

import android.content.Context;
import com.centralplay.app.model.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CatalogRepository {
    public interface Callback { void onResult(Catalog catalog, String source, String error); }
    private static volatile CatalogRepository INSTANCE;
    private final Context context;
    private final OkHttpClient http = new OkHttpClient.Builder().followRedirects(true).followSslRedirects(true).build();
    private final Gson gson = new Gson();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final File cacheFile;

    private CatalogRepository(Context c) { context=c.getApplicationContext(); cacheFile=new File(context.getFilesDir(), "catalog_cache.json"); }
    public static CatalogRepository get(Context c) { if(INSTANCE==null) synchronized(CatalogRepository.class){ if(INSTANCE==null) INSTANCE=new CatalogRepository(c); } return INSTANCE; }

    public void refreshAsync(Callback cb) {
        executor.execute(() -> {
            Result r=refreshBlocking();
            android.os.Handler h=new android.os.Handler(android.os.Looper.getMainLooper());
            h.post(() -> cb.onResult(r.catalog, r.source, r.error));
        });
    }

    public Result refreshBlocking() {
        String manifestUrl=Prefs.manifestUrl(context);
        if (manifestUrl==null || manifestUrl.trim().isEmpty()) {
            Catalog c=loadFallback();
            writeCache(c);
            Prefs.setSyncState(context,"Demostración integrada","");
            return new Result(c,"Demostración integrada","");
        }
        try {
            CentralManifest manifest=gson.fromJson(fetchText(manifestUrl), CentralManifest.class);
            if(manifest==null) throw new IOException("Manifest vacío");
            Catalog catalog;
            if(manifest.catalog!=null && !manifest.catalog.trim().isEmpty()) {
                catalog=fetchCatalogWithBackups(manifest.catalog, manifest.catalogBackups);
            } else {
                catalog=new Catalog();
                catalog.live=fetchM3u(manifest.liveM3u, manifest.liveBackups);
                if(manifest.moviesJson!=null && !manifest.moviesJson.trim().isEmpty()) {
                    Type t=new TypeToken<List<MediaEntry>>(){}.getType();
                    List<MediaEntry> list=gson.fromJson(fetchText(manifest.moviesJson),t); if(list!=null) catalog.movies=list;
                }
                if(manifest.seriesJson!=null && !manifest.seriesJson.trim().isEmpty()) {
                    Type t=new TypeToken<List<SeriesEntry>>(){}.getType();
                    List<SeriesEntry> list=gson.fromJson(fetchText(manifest.seriesJson),t); if(list!=null) catalog.series=list;
                }
                if(manifest.epgXmltv!=null && !manifest.epgXmltv.trim().isEmpty()) {
                    catalog.epg=fetchEpgWithBackups(manifest.epgXmltv,manifest.epgBackups);
                    XmlTvParser.applyNowNext(catalog.live,catalog.epg);
                }
                catalog.appUpdate=manifest.appUpdate;
                catalog.updatedAt=new Date().toString();
            }
            if(catalog.appUpdate==null) catalog.appUpdate=manifest.appUpdate;
            sanitize(catalog);
            writeCache(catalog);
            Prefs.setSyncState(context,"Remoto: "+hostLabel(manifestUrl),"");
            return new Result(catalog,"Remoto: "+hostLabel(manifestUrl),"");
        } catch(Exception e) {
            Catalog cached=readCache();
            if(cached!=null) {
                Prefs.setSyncState(context,"Caché local",e.getMessage());
                return new Result(cached,"Caché local",e.getMessage());
            }
            Catalog fallback=loadFallback();
            Prefs.setSyncState(context,"Demostración integrada",e.getMessage());
            return new Result(fallback,"Demostración integrada",e.getMessage());
        }
    }

    public Catalog getCachedCatalog() {
        Catalog c=readCache();
        if(c==null) c=loadFallback();
        sanitize(c); return c;
    }
    public void clearCache(){ if(cacheFile.exists()) cacheFile.delete(); }

    private Catalog fetchCatalogWithBackups(String main,List<String> backups) throws Exception {
        List<String> urls=new ArrayList<>(); urls.add(main); if(backups!=null)urls.addAll(backups);
        Exception last=null;
        for(String u:urls){ try { Catalog c=gson.fromJson(fetchText(u),Catalog.class); if(c!=null)return c; } catch(Exception e){last=e;} }
        throw last==null?new IOException("No hay fuentes de catálogo"):last;
    }
    private List<MediaEntry> fetchM3u(String main,List<String> backups) throws Exception {
        if(main==null||main.trim().isEmpty()) return new ArrayList<>();
        return M3uParser.parse(fetchWithBackups(main,backups));
    }
    private String fetchWithBackups(String main,List<String> backups) throws Exception {
        List<String> urls=new ArrayList<>(); if(main!=null&&!main.trim().isEmpty())urls.add(main); if(backups!=null)urls.addAll(backups);
        Exception last=null;
        for(String u:urls){ try{return fetchText(u);}catch(Exception e){last=e;} }
        throw last==null?new IOException("No hay fuentes"):last;
    }
    private List<EpgEvent> fetchEpgWithBackups(String main,List<String> backups) throws Exception {
        List<String> urls=new ArrayList<>();
        if(main!=null&&!main.trim().isEmpty()) urls.add(main);
        if(backups!=null) urls.addAll(backups);
        Exception last=null;
        for(String u:urls){
            try {
                Request request=new Request.Builder().url(u).header("User-Agent","CentralPlay/1.0").build();
                try(Response resp=http.newCall(request).execute()){
                    if(!resp.isSuccessful()) throw new IOException("HTTP "+resp.code()+" en "+hostLabel(u));
                    if(resp.body()==null) throw new IOException("EPG vacía");
                    try(InputStream decoded=decodeMaybeGzip(resp.body().byteStream(),u)){
                        List<EpgEvent> parsed=XmlTvParser.parse(decoded);
                        if(parsed.isEmpty()) throw new IOException("EPG sin eventos válidos en "+hostLabel(u));
                        return parsed;
                    }
                }
            } catch(Exception e){ last=e; }
        }
        throw last==null?new IOException("No hay fuentes EPG"):last;
    }

    private InputStream decodeMaybeGzip(InputStream raw,String url) throws IOException {
        PushbackInputStream in=new PushbackInputStream(new BufferedInputStream(raw),2);
        byte[] signature=new byte[2];
        int read=in.read(signature);
        if(read>0) in.unread(signature,0,read);
        boolean magic=read==2 && (signature[0]&0xff)==0x1f && (signature[1]&0xff)==0x8b;
        // OkHttp transparently decodes HTTP Content-Encoding:gzip. Magic-byte detection
        // also handles raw .xml.gz files served as application/gzip without double decoding.
        return magic ? new GZIPInputStream(in) : in;
    }
    private String fetchText(String url) throws Exception {
        Request r=new Request.Builder().url(url).header("User-Agent","CentralPlay/1.0").build();
        try(Response resp=http.newCall(r).execute()){
            if(!resp.isSuccessful())throw new IOException("HTTP "+resp.code()+" en "+hostLabel(url));
            if(resp.body()==null)throw new IOException("Respuesta vacía");
            return resp.body().string();
        }
    }
    private void sanitize(Catalog c){
        if(c.live==null)c.live=new ArrayList<>(); if(c.movies==null)c.movies=new ArrayList<>(); if(c.series==null)c.series=new ArrayList<>(); if(c.epg==null)c.epg=new ArrayList<>();
        for(MediaEntry m:c.live){m.type="live"; if(m.backupUrls==null)m.backupUrls=new ArrayList<>();}
        for(MediaEntry m:c.movies){m.type="movie"; if(m.backupUrls==null)m.backupUrls=new ArrayList<>();}
        for(SeriesEntry s:c.series){if(s.episodes==null)s.episodes=new ArrayList<>(); for(Episode e:s.episodes)if(e.backupUrls==null)e.backupUrls=new ArrayList<>();}
    }
    private Catalog loadFallback(){
        try(InputStream in=context.getAssets().open("catalog_fallback.json")){ return gson.fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),Catalog.class); }catch(Exception e){return new Catalog();}
    }
    private void writeCache(Catalog c){ try(FileWriter w=new FileWriter(cacheFile,false)){gson.toJson(c,w);}catch(Exception ignored){} }
    private Catalog readCache(){ if(!cacheFile.exists())return null; try(FileReader r=new FileReader(cacheFile)){return gson.fromJson(r,Catalog.class);}catch(Exception e){return null;} }
    private static String hostLabel(String url){ try{return new java.net.URI(url).getHost();}catch(Exception e){return url;} }

    public static class Result { public final Catalog catalog; public final String source; public final String error; Result(Catalog c,String s,String e){catalog=c;source=s;error=e==null?"":e;} }
}
