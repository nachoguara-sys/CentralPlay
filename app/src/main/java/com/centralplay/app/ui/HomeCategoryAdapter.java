package com.centralplay.app.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.centralplay.app.R;
import com.centralplay.app.model.MediaEntry;
import com.centralplay.app.model.SeriesEntry;
import java.util.ArrayList;
import java.util.List;

public class HomeCategoryAdapter extends RecyclerView.Adapter<HomeCategoryAdapter.Holder> {
    public interface MediaClick { void open(MediaEntry media); }
    public interface SeriesClick { void open(SeriesEntry series); }

    public static class HomeRow {
        final String title;
        final List<MediaEntry> media;
        final List<SeriesEntry> series;
        private HomeRow(String title,List<MediaEntry> media,List<SeriesEntry> series){this.title=title;this.media=media;this.series=series;}
        public static HomeRow media(String title,List<MediaEntry> media){return new HomeRow(title,media,new ArrayList<>());}
        public static HomeRow series(String title,List<SeriesEntry> series){return new HomeRow(title,new ArrayList<>(),series);}
    }

    private final Context context;
    private final MediaClick mediaClick;
    private final SeriesClick seriesClick;
    private final List<HomeRow> rows=new ArrayList<>();

    public HomeCategoryAdapter(Context context,MediaClick mediaClick,SeriesClick seriesClick){this.context=context;this.mediaClick=mediaClick;this.seriesClick=seriesClick;}

    public void setRows(List<HomeRow> incoming){
        rows.clear();
        if(incoming!=null){for(HomeRow row:incoming){if((row.media!=null&&!row.media.isEmpty())||(row.series!=null&&!row.series.isEmpty()))rows.add(row);}}
        notifyDataSetChanged();
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent,int viewType){
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_row,parent,false));
    }

    @Override public void onBindViewHolder(@NonNull Holder holder,int position){
        HomeRow row=rows.get(position);
        holder.title.setText(row.title);
        holder.list.setLayoutManager(new LinearLayoutManager(context,RecyclerView.HORIZONTAL,false));
        holder.list.setAdapter(new HomeContentAdapter(context,row.media,row.series,mediaClick,seriesClick));
    }

    @Override public int getItemCount(){return rows.size();}

    static class Holder extends RecyclerView.ViewHolder{
        final TextView title;
        final RecyclerView list;
        Holder(View item){super(item);title=item.findViewById(R.id.txt_category_title);list=item.findViewById(R.id.recycler_channels_horizontal);}
    }
}
