package com.centralplay.app.ui;

import android.app.*;
import android.app.DownloadManager;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.util.Rational;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.*;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.datasource.okhttp.OkHttpDataSource;
import okhttp3.OkHttpClient;
import androidx.media3.ui.PlayerView;
import com.centralplay.app.R;
import com.centralplay.app.model.MediaEntry;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import java.util.*;

public class PlayerActivity extends AppCompatActivity {
    private PlayerView view; private ExoPlayer player; private MediaEntry media; private List<String> urls; private int urlIndex=0;
    private LinearLayout errorPanel; private TextView errorText; private LinearLayout trackPanel,trackOptions; private TextView trackPanelTitle;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);setContentView(R.layout.activity_player);
        media=new Gson().fromJson(getIntent().getStringExtra("media"),MediaEntry.class);if(media==null){finish();return;}
        urls=media.allUrls();view=findViewById(R.id.player_view);errorPanel=findViewById(R.id.error_panel);errorText=findViewById(R.id.error_text);
        trackPanel=findViewById(R.id.track_panel);trackOptions=findViewById(R.id.track_options);trackPanelTitle=findViewById(R.id.track_panel_title);
        findViewById(R.id.retry_button).setOnClickListener(v->play(urlIndex));findViewById(R.id.tab_audio).setOnClickListener(v->showAudioPanel());findViewById(R.id.tab_subtitles).setOnClickListener(v->showSubtitlePanel());findViewById(R.id.tab_quality).setOnClickListener(v->showQualityPanel());
        MaterialToolbar tb=findViewById(R.id.player_toolbar);tb.setTitle(media.title);tb.setNavigationOnClickListener(v->finish());tb.inflateMenu(R.menu.menu_player);tb.setOnMenuItemClickListener(this::menu);
    }
    @Override protected void onStart(){super.onStart();initialize();}
    @Override protected void onStop(){super.onStop();if(!isInPictureInPictureMode())release();}
    @Override protected void onDestroy(){release();super.onDestroy();}
    @Override public void onBackPressed(){if(trackPanel!=null&&trackPanel.getVisibility()==View.VISIBLE)hideTrackPanel();else super.onBackPressed();}

    private void initialize(){
        if(player!=null)return;player=buildPlayer();view.setPlayer(player);
        player.addListener(new Player.Listener(){@Override public void onPlayerError(PlaybackException e){if(urlIndex+1<urls.size()){urlIndex++;Toast.makeText(PlayerActivity.this,"Fuente alternativa "+(urlIndex+1),Toast.LENGTH_SHORT).show();play(urlIndex);}else showError("No se pudo reproducir ninguna fuente.\n"+e.getErrorCodeName());}});
        play(urlIndex);
    }
    @UnstableApi private ExoPlayer buildPlayer(){
        OkHttpClient httpClient=new OkHttpClient.Builder().addInterceptor(chain->{okhttp3.Request.Builder b=chain.request().newBuilder();if(media.playbackHeaders!=null)for(Map.Entry<String,String> h:media.playbackHeaders.entrySet())if(h.getKey()!=null&&!h.getKey().trim().isEmpty()&&h.getValue()!=null&&!h.getValue().trim().isEmpty())b.header(h.getKey(),h.getValue());return chain.proceed(b.build());}).build();
        OkHttpDataSource.Factory ds=new OkHttpDataSource.Factory(httpClient);
        return new ExoPlayer.Builder(this).setMediaSourceFactory(new androidx.media3.exoplayer.source.DefaultMediaSourceFactory(this).setDataSourceFactory(ds)).build();
    }
    private void play(int index){errorPanel.setVisibility(View.GONE);if(urls==null||urls.isEmpty()){showError("Este contenido no tiene URL de reproducción.");return;}urlIndex=Math.max(0,Math.min(index,urls.size()-1));player.stop();player.clearMediaItems();player.setMediaItem(MediaItem.fromUri(urls.get(urlIndex)));player.prepare();player.setPlayWhenReady(true);com.centralplay.app.data.HistoryStore.add(this,media);}
    private void showError(String s){errorText.setText(s);errorPanel.setVisibility(View.VISIBLE);}
    private boolean menu(MenuItem item){int id=item.getItemId();if(id==R.id.action_pip){enterPip();return true;}if(id==R.id.action_tracks){showAudioPanel();return true;}if(id==R.id.action_download){download();return true;}return false;}
    private void enterPip(){if(Build.VERSION.SDK_INT>=26)try{enterPictureInPictureMode(new PictureInPictureParams.Builder().setAspectRatio(new Rational(16,9)).build());}catch(Exception e){Toast.makeText(this,"PiP no disponible",Toast.LENGTH_SHORT).show();}}
    private void showPanel(String title){trackPanelTitle.setText(title);trackPanel.setVisibility(View.VISIBLE);trackPanel.bringToFront();}
    private void hideTrackPanel(){trackPanel.setVisibility(View.GONE);view.requestFocus();}
    private void clearOptions(){trackOptions.removeAllViews();}
    private TextView addOption(String label,boolean selected,Runnable action){TextView option=new TextView(this);option.setText((selected?"✓  ":"    ")+label);option.setTextColor(Color.WHITE);option.setTextSize(16f);option.setGravity(Gravity.CENTER_VERTICAL);option.setPadding(dp(16),dp(12),dp(12),dp(12));option.setFocusable(true);option.setClickable(true);option.setBackgroundResource(R.drawable.selector_track_option);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);lp.setMargins(0,dp(4),0,dp(4));option.setLayoutParams(lp);option.setOnFocusChangeListener((v,has)->v.animate().scaleX(has?1.035f:1f).scaleY(has?1.035f:1f).setDuration(120).start());option.setOnClickListener(v->{action.run();hideTrackPanel();});trackOptions.addView(option);return option;}

    @UnstableApi private void showAudioPanel(){if(player==null)return;showPanel("Audio");clearOptions();TrackSelectionParameters params=player.getTrackSelectionParameters();addOption("Automático / Original",params.preferredAudioLanguages==null||params.preferredAudioLanguages.isEmpty(),()->player.setTrackSelectionParameters(player.getTrackSelectionParameters().buildUpon().clearOverridesOfType(C.TRACK_TYPE_AUDIO).setPreferredAudioLanguage(null).build()));Tracks tracks=player.getCurrentTracks();Set<String> seen=new HashSet<>();for(Tracks.Group g:tracks.getGroups()){if(g.getType()!=C.TRACK_TYPE_AUDIO)continue;for(int i=0;i<g.length;i++){if(!g.isTrackSupported(i))continue;Format f=g.getTrackFormat(i);String lang=f.language,label=f.label;String key=(lang==null?"":lang)+"|"+(label==null?"":label);if(seen.contains(key))continue;seen.add(key);String display=audioLabel(f,i);final TrackGroup group=g.getMediaTrackGroup();final int idx=i;addOption(display,g.isTrackSelected(i),()->applyOverride(C.TRACK_TYPE_AUDIO,group,idx));}}if(trackOptions.getChildCount()==1)addDisabledMessage("No hay pistas de audio alternativas");focusFirstOption();}
    @UnstableApi private void showSubtitlePanel(){if(player==null)return;showPanel("Subtítulos");clearOptions();boolean disabled=player.getTrackSelectionParameters().disabledTrackTypes.contains(C.TRACK_TYPE_TEXT);addOption("Desactivados",disabled,()->player.setTrackSelectionParameters(player.getTrackSelectionParameters().buildUpon().setTrackTypeDisabled(C.TRACK_TYPE_TEXT,true).clearOverridesOfType(C.TRACK_TYPE_TEXT).build()));Tracks tracks=player.getCurrentTracks();int count=0;for(Tracks.Group g:tracks.getGroups()){if(g.getType()!=C.TRACK_TYPE_TEXT)continue;for(int i=0;i<g.length;i++){if(!g.isTrackSupported(i))continue;Format f=g.getTrackFormat(i);final TrackGroup group=g.getMediaTrackGroup();final int idx=i;String label=(f.label!=null&&!f.label.isEmpty()?f.label:(f.language!=null?languageName(f.language):"Subtítulo "+(i+1)));addOption(label,g.isTrackSelected(i),()->applyOverride(C.TRACK_TYPE_TEXT,group,idx));count++;}}if(count==0)addDisabledMessage("Este contenido no ofrece subtítulos");focusFirstOption();}
    @UnstableApi private void showQualityPanel(){if(player==null)return;showPanel("Calidad");clearOptions();addOption("Automática (recomendada)",false,()->player.setTrackSelectionParameters(player.getTrackSelectionParameters().buildUpon().clearOverridesOfType(C.TRACK_TYPE_VIDEO).setTrackTypeDisabled(C.TRACK_TYPE_VIDEO,false).build()));Tracks tracks=player.getCurrentTracks();List<TrackChoice> opts=new ArrayList<>();for(Tracks.Group g:tracks.getGroups()){if(g.getType()!=C.TRACK_TYPE_VIDEO)continue;for(int i=0;i<g.length;i++){if(!g.isTrackSupported(i))continue;Format f=g.getTrackFormat(i);String label=(f.height>0?f.height+"p":"Video")+(f.bitrate>0?" · "+(f.bitrate/1000000f)+" Mbps":"");opts.add(new TrackChoice(g.getMediaTrackGroup(),i,label,g.isTrackSelected(i)));}}opts.sort((a,b)->Integer.compare(videoHeight(b.group,b.index),videoHeight(a.group,a.index)));for(TrackChoice c:opts)addOption(c.label,c.selected,()->applyOverride(C.TRACK_TYPE_VIDEO,c.group,c.index));if(opts.isEmpty())addDisabledMessage("No hay calidades alternativas");focusFirstOption();}
    private void addDisabledMessage(String text){TextView t=new TextView(this);t.setText(text);t.setTextColor(getColor(R.color.cp_text_muted));t.setPadding(dp(12),dp(16),dp(12),dp(16));trackOptions.addView(t);}
    private void focusFirstOption(){trackPanel.post(()->{if(trackOptions.getChildCount()>0)trackOptions.getChildAt(0).requestFocus();});}
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    private static int videoHeight(TrackGroup g,int i){return g.getFormat(i).height;}
    private String audioLabel(Format f,int i){String l=f.label,lang=f.language;String name=lang==null?null:languageName(lang);if(l!=null&&!l.trim().isEmpty()&&name!=null)return name+" · "+l;if(name!=null)return name+(lang!=null?" ("+lang+")":"");if(l!=null&&!l.trim().isEmpty())return l;return "Pista "+(i+1);}
    private String languageName(String tag){Locale locale=Locale.forLanguageTag(tag);String s=locale.getDisplayLanguage(Locale.getDefault());return s==null||s.trim().isEmpty()?tag:s;}
    @UnstableApi private void applyOverride(int type,TrackGroup group,int index){TrackSelectionParameters.Builder b=player.getTrackSelectionParameters().buildUpon().setTrackTypeDisabled(type,false).clearOverridesOfType(type).addOverride(new TrackSelectionOverride(group,Collections.singletonList(index)));player.setTrackSelectionParameters(b.build());}
    private void download(){if(urls==null||urls.isEmpty())return;String u=urls.get(urlIndex);if(u.contains(".m3u8")){Toast.makeText(this,"La descarga HLS offline requiere un módulo de descarga dedicado.",Toast.LENGTH_LONG).show();return;}try{DownloadManager dm=(DownloadManager)getSystemService(DOWNLOAD_SERVICE);DownloadManager.Request r=new DownloadManager.Request(Uri.parse(u)).setTitle(media.title).setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED).setDestinationInExternalFilesDir(this,android.os.Environment.DIRECTORY_DOWNLOADS,safe(media.title)+".mp4");dm.enqueue(r);Toast.makeText(this,"Descarga iniciada",Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(this,"No se pudo iniciar la descarga",Toast.LENGTH_LONG).show();}}
    private static String safe(String s){return s==null?"video":s.replaceAll("[^a-zA-Z0-9._ -]","_");}
    private void release(){if(player!=null){player.release();player=null;view.setPlayer(null);}}
    private static class TrackChoice{final TrackGroup group;final int index;final String label;final boolean selected;TrackChoice(TrackGroup g,int i,String l,boolean s){group=g;index=i;label=l;selected=s;}}
}
