package com.brightcontroller.allex.brightcontroller;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.FileDescriptor;

/**
 * Created by allex on 24/06/17.
 */

public class LuminosityWatcherService extends Service {

    private final String LOG_TAG = "luminositywatcher";

    private WatcherThread watcherThread;
    private Context appContext = getBaseContext();

    private final int NOTIFICATION_ID = 8991;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        watcherThread = WatcherThread.getInstance(this);

        Log.i(LOG_TAG, "Criou o serviço");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(!watcherThread.isCompatible()){
            Toaster.showToast("Seu dispositivo não é compatível com o Bright Controller.", appContext);
        }
        else{
            if(!watcherThread.isAlive()) {
                if(null != watcherThread){
                    watcherThread.stopThread();
                    Log.i(LOG_TAG, "Parou o thread que existia");
                }
                watcherThread.startThread();
                Log.i(LOG_TAG, "Iniciou o thread");
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(watcherThread.isAlive()) {
            watcherThread.stopThread();
            watcherThread.interrupt();
            watcherThread.killInstance();
        }

    }

}
