package com.brightcontroller.allex.brightcontroller;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;


/**
 * Implementation of App Widget functionality.
 */
public class BrightControllerWidget extends AppWidgetProvider {

    private static final String LOG_TAG = "brightcontrollerwidget";

    private static final String INTENT_ACTION = "intent.action.update_status";
    private static final String SERVICE_ON = "ON";
    private static final String SERVICE_OFF = "OFF";

    private static boolean startService = false;
    private static boolean hasPermission = false;

    private ManagePreferences managePreferences;
    private static GerenciadorNotificacoes not = new GerenciadorNotificacoes();

    //Preferências
    private boolean rememberBrightness = true;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        ManagePreferences prefs = new ManagePreferences(context);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.bright_controller_widget);

        if(startService){
            views.setTextViewText(R.id.btn_controller_status, SERVICE_OFF);
        }
        else{
            views.setTextViewText(R.id.btn_controller_status, SERVICE_ON);
        }

        //Intent com uma ação que será recebida pelo próprio widget
        Intent intent = new Intent(context, BrightControllerWidget.class);
        intent.setAction(INTENT_ACTION);
        //PendinIntent que carrega o intent acima e dispara quando a TextView é clicada
        PendingIntent toggleIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_controller_status, toggleIntent);

        //PendingIntent que abre a tela de configurações
        PendingIntent prefsIntent = PendingIntent.getActivity(context, 0, new Intent(context, PreferencesActivity.class), 0);
        views.setOnClickPendingIntent(R.id.btn_preferences, prefsIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private void loadPreferences(Context context){
        if(null == managePreferences){
            managePreferences = new ManagePreferences(context);
        }

        rememberBrightness = managePreferences.getRememberBrightness();

        Log.i(LOG_TAG, "Carregou preferências:\n\trememberBrightness = " + String.valueOf(rememberBrightness));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        loadPreferences(context);

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created


        loadPreferences(context);

        if(rememberBrightness) {
            saveBrightness(context);
        }

        Log.i(LOG_TAG, "Iniciou o widget");

        if(checkSystemWritePermission(context)){
            hasPermission = true;
            Log.i(LOG_TAG, "Tem permisão de escrita");
        }
        else{
           //Toaster.showToast("Sem premissão de escrita.", context);
            hasPermission = false;
            Log.i(LOG_TAG, "Sem premissão de escrita.");
        }

        not.showNotification(context);

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        not.closeNotification();

        //Finalizar o serviço aqui
        Intent intent = new Intent(context, LuminosityWatcherService.class);
        context.stopService(intent);

        if(rememberBrightness) {
            restoreBrightness(context);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if(INTENT_ACTION.equals(intent.getAction())){

            if(null == managePreferences){
                managePreferences = new ManagePreferences(context);
            }

            Log.i(LOG_TAG, "Pegou clique");

            if(!hasPermission){
                hasPermission = checkSystemWritePermission(context);
            }

            AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);

            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.bright_controller_widget);
            ComponentName cp = new ComponentName(context, BrightControllerWidget.class);

            if(startService){
                //Começa o serviço
                startService = false;

                //Toast.makeText(context, "OFF", Toast.LENGTH_SHORT).show();
                views.setTextViewText(R.id.btn_controller_status, SERVICE_ON);
                managePreferences.setIsRunning(false);

                if(rememberBrightness) {
                    restoreBrightness(context);
                }

                stopWatcher(context);
                Log.i(LOG_TAG, "Parou a aplicação");
            }
            else{
                startService = true;
                //Finaliza o serviço

                //Toast.makeText(context, "ON", Toast.LENGTH_SHORT).show();
                views.setTextViewText(R.id.btn_controller_status, SERVICE_OFF);
                managePreferences.setIsRunning(true);

                if(rememberBrightness) {
                    saveBrightness(context);
                }

                startWatcher(context);
                Log.i(LOG_TAG, "Retomou a aplicação");
            }

            not.updateNotification(context);
            Log.i(LOG_TAG, "Atualizou notificação");

            Log.i(LOG_TAG, "Status:\nstartService = " + String.valueOf(startService) + "\nhasPermission = " + String.valueOf(hasPermission) + "\nrememberBrightness = " + String.valueOf(rememberBrightness));

            widgetManager.updateAppWidget(cp, views);

        }
    }

    private void startWatcher(Context context){
        if(hasPermission){
            Intent serviceStart = new Intent(context, LuminosityWatcherService.class);
            context.startService(serviceStart);
        }
    }

    private void stopWatcher(Context context){
        Intent serviceStop = new Intent(context, LuminosityWatcherService.class);
        context.stopService(serviceStop);
    }

    private boolean checkSystemWritePermission(Context context) {
        boolean retVal = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(context)) {
                retVal = true;
            }
            else {
                Log.i(LOG_TAG, "Começando pedido de permissão");
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                Log.i(LOG_TAG, "Mostrou pedido de permissão");
            }
        }
        return retVal;
    }

    private void saveBrightness(Context context){
        try {
            if(null == managePreferences){
                managePreferences = new ManagePreferences(context);
            }

            int curBrightnessValue = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            managePreferences.setBrightnessLevel(curBrightnessValue);

            Log.i(LOG_TAG, "Salvou o brilho: " + String.valueOf(curBrightnessValue));
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void restoreBrightness(Context context){
        if(null == managePreferences){
            managePreferences = new ManagePreferences(context);
        }

        int brightness = managePreferences.getBrightnessLevel();

        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);

        Log.i(LOG_TAG, "Restaurou o brilho: " + String.valueOf(brightness));

    }

}

