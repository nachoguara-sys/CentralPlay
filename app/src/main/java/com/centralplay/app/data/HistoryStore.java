package com.centralplay.app.data;

import android.content.Context;
import com.centralplay.app.model.MediaEntry;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.*;

public final class HistoryStore {
    private static final String NAME="central_play_history";
    private static final Gson GSON=new Gson();
    private HistoryStore(){}

    public static synchronized void add(Context c, MediaEntry item){
        if(item==null||item.id==null||item.id.trim().isEmpty())return;
        List<MediaEntry> list=get(c);
        list.removeIf(x->item.id.equals(x.id));
        list.add(0,item);
        if(list.size()>50) list=new ArrayList<>(list.subList(0,50));
        c.getSharedPreferences(NAME,Context.MODE_PRIVATE).edit().putString("items",GSON.toJson(list)).apply();
    }
    public static List<MediaEntry> get(Context c){
        String json=c.getSharedPreferences(NAME,Context.MODE_PRIVATE).getString("items","[]");
        try{Type t=new TypeToken<List<MediaEntry>>(){}.getType();List<MediaEntry> l=GSON.fromJson(json,t);return l==null?new ArrayList<>():new ArrayList<>(l);}catch(Exception e){return new ArrayList<>();}
    }
    public static void clear(Context c){c.getSharedPreferences(NAME,Context.MODE_PRIVATE).edit().clear().apply();}
}
