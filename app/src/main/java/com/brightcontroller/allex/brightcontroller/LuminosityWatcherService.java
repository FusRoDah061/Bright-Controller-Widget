package com.brightcontroller.allex.brightcontroller;

import android.app.Notification;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.util.Log;

/**
 * Created by allex on 24/06/17.
 */

public class LuminosityWatcherService extends Service implements Thread.UncaughtExceptionHandler {

    private final String LOG_TAG = "luminositywatcher";
    private final int serviceId = 1;

    private WatcherThread watcherThread;
    private Context appContext = getBaseContext();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(serviceId, this.buildServiceNotification());
        watcherThread = WatcherThread.getInstance(this);

        watcherThread.setUncaughtExceptionHandler(this);

        Log.i(LOG_TAG, "Criou o serviço");
    }

    private Notification buildServiceNotification() {
        String channelId = "SERVICE";
        NotificationChannelCompat channel = new NotificationChannelCompat.Builder(channelId, 3)
            .setName("Serviço")
            .setDescription("Notificação do serviço de monitoramento")
            .build();

        NotificationManagerCompat.from(this).createNotificationChannel(channel);

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Monitorando luz")
                .setContentText("BrightController está monitorando a luminosidade do ambiente.")
                .build();
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

    @Override
    public void uncaughtException(Thread t, Throwable e) {



        //if(e.getMessage().toLowerCase().contains("android.permission.write_settings")){
            //Toast.makeText(this,"Sem permissão para alterar o brilho. Parando BrightController", Toast.LENGTH_LONG).show();


            Log.i(LOG_TAG, "Thread crashou: " + e.getMessage());
            //Atualiza o widget
            new ManagePreferences(this).setIsRunning(false);

            Intent intent = new Intent(this, BrightControllerWidget.class);
            intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
            int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), BrightControllerWidget.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
            sendBroadcast(intent);
        //}


    }
}
