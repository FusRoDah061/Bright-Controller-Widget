package com.brightcontroller.allex.brightcontroller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by allex on 14/12/17.
 */

public class GerenciadorNotificacoes {

    private static final String LOG_TAG = "brightcontrollerwidget";
    private final int NOTIFICATION_ID = 1995463;
    private Context context;
    private static GerenciadorNotificacoes instance = null;
    private NotificationManager notificationManager;

    private GerenciadorNotificacoes(Context context){
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static GerenciadorNotificacoes getInstance(Context context){
        if(instance == null){
            instance = new GerenciadorNotificacoes(context);
        }

        return instance;
    }

    public void showNotification(){
        ManagePreferences prefs = new ManagePreferences(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("Bright Controller");
        builder.setOngoing(true);
        builder.setLocalOnly(true);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_SERVICE);
        }

        if(prefs.getBoolean(ManagePreferences.PREFS_IS_RUNNING)) {
            builder.setContentText("Executando, toque para pausar");
            builder.setSmallIcon(R.drawable.ic_notification_controller_on);
        }
        else{
            builder.setContentText("Pausado, toque para retomar");
            builder.setSmallIcon(R.drawable.ic_notification_controller_off);
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void closeNotification(){
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public void updateNotification(boolean running){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("Bright Controller");
        builder.setOngoing(true);
        builder.setLocalOnly(true);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_SERVICE);
        }

        if(running) {
            builder.setContentText("Executando, toque para pausar");
            builder.setSmallIcon(R.drawable.ic_notification_controller_on);
        }
        else{
            builder.setContentText("Pausado, toque para retomar");
            builder.setSmallIcon(R.drawable.ic_notification_controller_off);
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

}
