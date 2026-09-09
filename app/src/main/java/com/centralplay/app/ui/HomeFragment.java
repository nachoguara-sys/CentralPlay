package com.centralplay.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.centralplay.app.R;
import com.centralplay.app.data.CatalogRepository;
import com.centralplay.app.data.ImageLoader;
import com.centralplay.app.data.Prefs;
import com.centralplay.app.model.Catalog;
import com.centralplay.app.model.MediaEntry;
import com.centralplay.app.model.SeriesEntry;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private ImageView heroImage;
    private TextView heroTitle, heroSubtitle, heroBadge, status;
    private RecyclerView categories;
    private HomeCategoryAdapter categoryAdapter;

    public HomeFragment(){ super(R.layout.fragment_home); }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state){
        heroImage=view.findViewById(R.id.img_hero_banner);
        heroTitle=view.findViewById(R.id.txt_hero_title);
        heroSubtitle=view.findViewById(R.id.txt_hero_subtitle);
        heroBadge=view.findViewById(R.id.txt_hero_badge);
        status=view.findViewById(R.id.status_text);
        categories=view.findViewById(R.id.recycler_view_categories);
        categories.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
        categoryAdapter=new HomeCategoryAdapter(requireContext(), this::openMedia, this::openSeries);
        categories.setAdapter(categoryAdapter);
        refresh();
    }

    public void refresh(){
        if(getContext()==null || categories==null) return;
        Catalog catalog=CatalogRepository.get(requireContext()).getCachedCatalog();
        MediaEntry featured=firstAllowed(catalog.live);
        if(featured==null) featured=firstAllowed(catalog.movies);
        if(featured!=null){
            heroTitle.setText(featured.title==null||featured.title.isBlank()?"Central Play":featured.title);
            heroSubtitle.setText(heroSubtitle(featured));
            heroBadge.setText("live".equals(featured.type)?"DESTACADO EN VIVO":"DESTACADO");
            ImageLoader.load(heroImage,featured.posterUrl);
            MediaEntry selected=featured;
            heroImage.setOnClickListener(v->openMedia(selected));
            heroImage.setFocusable(true);
        } else {
            heroTitle.setText("CENTRAL PLAY");
            heroSubtitle.setText("Tu entretenimiento, a tu manera");
            heroBadge.setText("CENTRAL PLAY");
        }
        String src=Prefs.syncSource(requireContext());
        status.setText(src==null||src.isBlank()?"Catálogo local":src);
        categoryAdapter.setRows(buildRows(catalog));
    }

    private List<HomeCategoryAdapter.HomeRow> buildRows(Catalog c){
        List<HomeCategoryAdapter.HomeRow> rows=new ArrayList<>();
        if(c==null) return rows;
        Map<String,List<MediaEntry>> liveGroups=new LinkedHashMap<>();
        for(MediaEntry m:c.live){
            if(m.adult && !Prefs.adultEnabled(requireContext())) continue;
            String group=(m.group==null||m.group.isBlank())?"TV en vivo":m.group;
            liveGroups.computeIfAbsent(group,k->new ArrayList<>()).add(m);
        }
        for(Map.Entry<String,List<MediaEntry>> e:liveGroups.entrySet()) rows.add(HomeCategoryAdapter.HomeRow.media(e.getKey(),e.getValue()));
        List<MediaEntry> movies=filterAdult(c.movies);
        if(!movies.isEmpty()) rows.add(HomeCategoryAdapter.HomeRow.media("Películas",movies));
        List<SeriesEntry> series=filterSeries(c.series);
        if(!series.isEmpty()) rows.add(HomeCategoryAdapter.HomeRow.series("Series",series));
        return rows;
    }

    private List<MediaEntry> filterAdult(List<MediaEntry> input){
        List<MediaEntry> out=new ArrayList<>(); if(input==null)return out; boolean allow=Prefs.adultEnabled(requireContext());
        for(MediaEntry m:input) if(allow||!m.adult) out.add(m); return out;
    }
    private List<SeriesEntry> filterSeries(List<SeriesEntry> input){
        List<SeriesEntry> out=new ArrayList<>(); if(input==null)return out; boolean allow=Prefs.adultEnabled(requireContext());
        for(SeriesEntry s:input) if(allow||!s.adult) out.add(s); return out;
    }
    private MediaEntry firstAllowed(List<MediaEntry> list){ if(list==null)return null; boolean allow=Prefs.adultEnabled(requireContext()); for(MediaEntry m:list)if(allow||!m.adult)return m;return null; }
    private String heroSubtitle(MediaEntry m){
        if("live".equals(m.type) && m.nowTitle!=null&&!m.nowTitle.isBlank()) return "Ahora: "+m.nowTitle;
        if(m.description!=null&&!m.description.isBlank()) return m.description;
        if(m.group!=null&&!m.group.isBlank()) return m.group;
        return "Central Play";
    }
    private void openMedia(MediaEntry media){ Intent i=new Intent(requireContext(),PlayerActivity.class);i.putExtra("media",new Gson().toJson(media));startActivity(i); }
    private void openSeries(SeriesEntry series){ Intent i=new Intent(requireContext(),SeriesDetailActivity.class);i.putExtra("series",new Gson().toJson(series));startActivity(i); }
}
