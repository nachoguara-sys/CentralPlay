package com.centralplay.app.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.centralplay.app.R;
import com.centralplay.app.data.ImageLoader;
import com.centralplay.app.model.MediaEntry;
import com.centralplay.app.model.SeriesEntry;
import java.util.List;

public class HomeContentAdapter extends RecyclerView.Adapter<HomeContentAdapter.Holder>{
    private final Context context;
    private final List<MediaEntry> media;
    private final List<SeriesEntry> series;
    private final HomeCategoryAdapter.MediaClick mediaClick;
    private final HomeCategoryAdapter.SeriesClick seriesClick;

    public HomeContentAdapter(Context context,List<MediaEntry> media,List<SeriesEntry> series,HomeCategoryAdapter.MediaClick mediaClick,HomeCategoryAdapter.SeriesClick seriesClick){
        this.context=context;this.media=media;this.series=series;this.mediaClick=mediaClick;this.seriesClick=seriesClick;setHasStableIds(true);
    }

    private boolean isSeries(){return series!=null&&!series.isEmpty();}
    @Override public long getItemId(int position){String id=isSeries()?series.get(position).id:media.get(position).id;return id==null?position:id.hashCode();}

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent,int viewType){
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_content,parent,false));
    }

    @Override public void onBindViewHolder(@NonNull Holder holder,int position){
        if(isSeries()){
            SeriesEntry item=series.get(position);
            holder.title.setText(item.title);
            holder.badge.setText("SERIE");
            ImageLoader.load(holder.poster,item.posterUrl);
            holder.itemView.setOnClickListener(v->seriesClick.open(item));
        }else{
            MediaEntry item=media.get(position);
            holder.title.setText(item.title);
            holder.badge.setText("live".equals(item.type)?"EN VIVO":"PELÍCULA");
            ImageLoader.load(holder.poster,item.posterUrl);
            holder.itemView.setOnClickListener(v->mediaClick.open(item));
        }
        holder.itemView.setOnFocusChangeListener((view,focused)->{
            view.clearAnimation();
            view.startAnimation(AnimationUtils.loadAnimation(context,focused?R.anim.scale_up:R.anim.scale_down));
            view.setElevation(focused?20f:4f);
        });
    }

    @Override public int getItemCount(){return isSeries()?series.size():(media==null?0:media.size());}

    static class Holder extends RecyclerView.ViewHolder{
        final ImageView poster;
        final TextView title,badge;
        Holder(View item){super(item);poster=item.findViewById(R.id.home_poster);title=item.findViewById(R.id.home_title);badge=item.findViewById(R.id.home_badge);}
    }
}
