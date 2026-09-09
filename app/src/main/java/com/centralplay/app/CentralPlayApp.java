package com.centralplay.app;

import android.app.Application;
import androidx.work.*;
import com.centralplay.app.data.SyncWorker;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class CentralPlayApp extends Application {
    @Override public void onCreate(){
        super.onCreate();
        scheduleDailySync();
    }

    /**
     * WorkManager is intentionally inexact. This schedules the first refresh near 03:00
     * local time and repeats roughly every 24 h, while Android remains free to defer it
     * for Doze/battery constraints.
     */
    private void scheduleDailySync(){
        Constraints constraints=new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        long initialDelay=millisUntilNextLocalHour(3);
        PeriodicWorkRequest request=new PeriodicWorkRequest.Builder(SyncWorker.class,24,TimeUnit.HOURS)
                .setInitialDelay(initialDelay,TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "centralplay_daily_catalog_epg_sync",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
        );
    }

    private static long millisUntilNextLocalHour(int hour){
        Calendar now=Calendar.getInstance();
        Calendar next=(Calendar)now.clone();
        next.set(Calendar.HOUR_OF_DAY,hour);
        next.set(Calendar.MINUTE,0);
        next.set(Calendar.SECOND,0);
        next.set(Calendar.MILLISECOND,0);
        if(!next.after(now)) next.add(Calendar.DAY_OF_YEAR,1);
        return next.getTimeInMillis()-now.getTimeInMillis();
    }
}
