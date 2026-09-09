package com.centralplay.app.data;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class SyncWorker extends Worker {
    public SyncWorker(@NonNull Context context,@NonNull WorkerParameters params){super(context,params);}
    @NonNull @Override public Result doWork(){
        CatalogRepository.Result r=CatalogRepository.get(getApplicationContext()).refreshBlocking();
        return r.catalog==null?Result.retry():Result.success();
    }
}
