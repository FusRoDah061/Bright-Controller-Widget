package com.brightcontroller.allex.brightcontroller;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;


/**
 * Implementation of App Widget functionality.
 */
public class BrightControllerWidget extends AppWidgetProvider {

    private static final String LOG_TAG = "brightcontrollerwidget";

    private static final String INTENT_ACTION = "intent.action.update_status";
    private static final String SERVICE_ON = "ON";
    private static final String SERVICE_OFF = "OFF";

    private static boolean isRunning = false;
    private static boolean hasPermission = false;

    private ManagePreferences managePreferences;

    //Preferências
    private boolean rememberBrightness = true;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        Log.i(LOG_TAG, "Atualizando widget");

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.bright_controller_widget);

        new ManagePreferences(context).setIsRunning(false);

        views.setTextViewText(R.id.btn_controller_status, SERVICE_ON);

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

        managePreferences.setIsRunning(false);
        rememberBrightness = managePreferences.getRememberBrightness();

        Log.i(LOG_TAG, "Carregou preferências:\n\trememberBrightness = " + String.valueOf(rememberBrightness));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i(LOG_TAG, "Caiu para atualizar o wdiget");

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

        /*if(checkSystemWritePermission(context)){
            hasPermission = true;
            Log.i(LOG_TAG, "Tem permisão de escrita");
        }
        else{
           //Toaster.showToast("Sem premissão de escrita.", context);
            hasPermission = false;
            Log.i(LOG_TAG, "Sem premissão de escrita.");
        }*/

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled

        //Finalizar o serviço aqui
        stopWatcher(context);

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

            hasPermission = checkSystemWritePermission(context);

            if(hasPermission) {
                AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);

                // Construct the RemoteViews object
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.bright_controller_widget);
                ComponentName cp = new ComponentName(context, BrightControllerWidget.class);

                isRunning = managePreferences.getIsRunning();

                if (isRunning) {
                    //Começa o serviço
                    isRunning = false;

                    //Toast.makeText(context, "OFF", Toast.LENGTH_SHORT).show();
                    views.setTextViewText(R.id.btn_controller_status, SERVICE_ON);

                    if (rememberBrightness) {
                        restoreBrightness(context);
                    }

                    stopWatcher(context);
                    Log.i(LOG_TAG, "Parou a aplicação");
                } else {
                    isRunning = true;
                    //Finaliza o serviço

                    //Toast.makeText(context, "ON", Toast.LENGTH_SHORT).show();
                    views.setTextViewText(R.id.btn_controller_status, SERVICE_OFF);

                    if (rememberBrightness) {
                        saveBrightness(context);
                    }

                    startWatcher(context);
                    Log.i(LOG_TAG, "Retomou a aplicação");
                }

                Log.i(LOG_TAG, "Status:\nisRunning = " + String.valueOf(isRunning) + "\nhasPermission = " + String.valueOf(hasPermission) + "\nrememberBrightness = " + String.valueOf(rememberBrightness));

                widgetManager.updateAppWidget(cp, views);
            }
            else{
                Toast.makeText(context,"Sem permissão para alterar o brilho.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startWatcher(Context context){
        if(hasPermission){
            managePreferences.setIsRunning(true);

            Intent serviceStart = new Intent(context, LuminosityWatcherService.class);
            context.startService(serviceStart);
        }
    }

    private void stopWatcher(Context context){

        new ManagePreferences(context).setIsRunning(false);

        Intent serviceStop = new Intent(context, LuminosityWatcherService.class);
        context.stopService(serviceStop);
    }

    private boolean checkSystemWritePermission(final Context context) {
        boolean retVal = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(context)) {
                retVal = true;
            }
            else {
                retVal = false;

                Log.i(LOG_TAG, "Começando pedido de permissão");

                Intent perm = new Intent(context, SolicitaPermissaoActivity.class);
                context.startActivity(perm);
            }
        }

        return retVal;
    }

    private void saveBrightness(Context context){
        hasPermission = checkSystemWritePermission(context);

        if(hasPermission) {
            try {
                if (null == managePreferences) {
                    managePreferences = new ManagePreferences(context);
                }

                int curBrightnessValue = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                managePreferences.setBrightnessLevel(curBrightnessValue);

                Log.i(LOG_TAG, "Salvou o brilho: " + String.valueOf(curBrightnessValue));
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    private void restoreBrightness(Context context){

        try {
            if (null == managePreferences) {
                managePreferences = new ManagePreferences(context);
            }

            int brightness = managePreferences.getBrightnessLevel();

            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);

            Log.i(LOG_TAG, "Restaurou o brilho: " + String.valueOf(brightness));
        }
        catch (Exception e){ e.printStackTrace(); }

    }

}

