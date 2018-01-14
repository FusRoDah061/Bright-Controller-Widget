package com.brightcontroller.allex.brightcontroller;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;

import static android.content.Context.POWER_SERVICE;

/**
 * Created by allex on 24/06/17.
 *
 * Classe rodando em um thread separado que monitora o sensor de luz e ajusta o brilho da tela de acordo.
 */

public class WatcherThread extends Thread implements SensorEventListener {

    private static final String LOG_TAG = "watcherthread";

    /**
     * Valor máximo do brilho da tela
     */
    private final int MAX_SCREEN_BRIGHTNESS = 255;
    /**
     * Valor para o brilho da tela mediano
     */
    private final int MEDIUM_SCREEN_BRIGHTNESS = 128;
    /**
     * Valor para o brilho da tela baixo
     */
    private final int LOW_SCREEN_BRIGHTNESS = 64;
    /**
     * Menor valor possível para o brilho da tela
     */
    private final int MIN_SCREEN_BRIGHTNESS = 0;
    /**
     * Limiar de luz quando se torna difícil enxergar o conteúdo da tela do dispositivo. Alta intensidade de luz
     */
    private final float HIGH_LIGHT = 200;
    /**
     * Limiar de luz quando se torna difícil enxergar o conteúdo da tela do dispositivo. Média intensidade de luz
     */
    private final float MEDIUM_LIGHT = 150;
    /**
     * Limiar de luz quando se torna difícil enxergar o conteúdo da tela do dispositivo. Baixa intensidade de luz
     */
    private final float LOW_LIGHT = 100;

    private boolean runThread = true;
    private boolean isCompatible = true;
    private float luxLevel = 0;

    //Preferências
    private long verifyInterval;
    private int brightnessMode;
    private boolean screenOff;

    private Context context;

    private SensorManager sensorManager;
    private Sensor lightSensor;

    private ManagePreferences managePreferences;

    private static WatcherThread instance = null;

    /**
     * Obtém o sensor de luminosidade e verifica se o dispositivo é compatível(e.g possui o sensor)
     * @param context contexto invocador
     */
    private WatcherThread(Context context){
        this.context = context;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        //Toaster.TshowToast("Obteve o sensor", context);
        Log.i(LOG_TAG, "Obteve o sensor");

        if(null == lightSensor){
            runThread = false;
            isCompatible = false;
            //Toaster.TshowToast("Não é compatível", context);
            Log.i(LOG_TAG, "Não é compatível");
        }
        else{
            loadPreferences(context);
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void loadPreferences(Context context){
        managePreferences = new ManagePreferences(context);

        verifyInterval = managePreferences.getInterval();
        brightnessMode = managePreferences.getBrightnessMode();
        screenOff = managePreferences.getRunOnScreenOff();
    }

    public void updatePreferences(){
        verifyInterval = managePreferences.getInterval();
        brightnessMode = managePreferences.getBrightnessMode();
        screenOff = managePreferences.getRunOnScreenOff();
    }

    /**
     * Retorna [e inicializa] a instancia única dessa classe
     * @param con contexto invocador
     * @return instancia única da classe
     */
    public static WatcherThread getInstance(Context con){
        if(null == instance){
            instance = new WatcherThread(con);
        }

        return instance;
    }

    public void killInstance(){
        instance = null;
    }

    public void run(){

        while(runThread){

            adjustScreenBrightness(getLuxValue());

            try {
                Thread.sleep(verifyInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Interrompe o thread e descarta o sensor
     */
    public void stopThread(){
        runThread = false;
        sensorManager.unregisterListener(this);

        //Toaster.TshowToast("Parou o thread", context);
        Log.i(LOG_TAG, "Parou o thread");
    }

    /**
     * Inicia o thread e registra o sensor
     */
    public void startThread(){
        runThread = true;
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        this.start();

        //Toaster.TshowToast("Começou o thread", context);
        Log.i(LOG_TAG, "Começou o thread");
    }

    /**
     * Ajusta o brilho da tela de acordo com o valor vindo do sensor de luminosidade.
     *
     * @param lux valor vindo do sensor de luminosidade
     */
    private void adjustScreenBrightness(float lux){

        int brightness = 0;

        Log.i(LOG_TAG, "Rodar apagada: " + screenOff + ", Acesa: " + isScreenOn());

        if(!screenOff && !isScreenOn()){
            return;
        }

        if(brightnessMode == ManagePreferences.VAL_BRIGHTNESS_MODE_LOW_HIGH){

            if(lux > HIGH_LIGHT){
                //Coloca o brilho no máximo
                brightness = MAX_SCREEN_BRIGHTNESS;
            }
            else if(lux > MEDIUM_LIGHT){
                //Coloca um brilho médio
                brightness = MEDIUM_SCREEN_BRIGHTNESS;
            }
            else if(lux > LOW_LIGHT){
                //Coloca o brilho no baixo
                brightness = LOW_SCREEN_BRIGHTNESS;
            }
            else if(lux < LOW_LIGHT){
                //Coloca o brilho no mínimo
                brightness = MIN_SCREEN_BRIGHTNESS;
            }

        }
        else if(brightnessMode == ManagePreferences.VAL_BRIGHTNESS_MODE_HIGH_LOW) {

            if(lux > HIGH_LIGHT){
                //Coloca o brilho no mínimo
                brightness = MIN_SCREEN_BRIGHTNESS;
            }
            else if(lux > MEDIUM_LIGHT){
                //Coloca um brilho baixo
                brightness = LOW_SCREEN_BRIGHTNESS;
            }
            else if(lux > LOW_LIGHT){
                //Coloca o brilho no médio
                brightness = MEDIUM_SCREEN_BRIGHTNESS;
            }
            else if(lux < LOW_LIGHT){
                //Coloca o brilho no máximo
                brightness = MAX_SCREEN_BRIGHTNESS;
            }

        }

        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
        //Toaster.TshowToast("Alterou o brilho. \nLux: " + String.valueOf(lux) + " \nBrilho: " + String.valueOf(brightness), context);
        Log.i(LOG_TAG, "Alterou o brilho. \nLux: " + String.valueOf(lux) + " \nBrilho: " + String.valueOf(brightness));

    }

    private boolean isScreenOn(){
        boolean isOn = true;

        try {
            PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
            isOn = Build.VERSION.SDK_INT>= Build.VERSION_CODES.KITKAT_WATCH&&powerManager.isInteractive()|| Build.VERSION.SDK_INT< Build.VERSION_CODES.KITKAT_WATCH&&powerManager.isScreenOn();
        } catch(Exception e) { e.printStackTrace(); Log.i(LOG_TAG, e.getMessage()); }

        return isOn;
    }

    /**
     * Obtém o valor salvo do sensor de luminosidade
     * @return o valor obtido do sensor de luz (em lux)
     */
    private float getLuxValue(){
        return luxLevel;
    }

    /**
     * Retorna o valor obtido inicialmente no thread que indica se o dispositivo é compatível ou não com o widget
     * @return true ou false
     */
    public boolean isCompatible(){
        return isCompatible;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        luxLevel = sensorEvent.values[0];
        //Toaster.TshowToast("valor do sensor mudou: " + String.valueOf(luxLevel), context);
        //Log.i(LOG_TAG, "Valor do sensor mudou: " + String.valueOf(luxLevel));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

}
