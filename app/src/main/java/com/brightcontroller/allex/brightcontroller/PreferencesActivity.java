package com.brightcontroller.allex.brightcontroller;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;

public class PreferencesActivity extends AppCompatActivity implements View.OnClickListener {

    private ManagePreferences managePreferences;

    private RadioButton rbLowHighBright;
    private RadioButton rbHighLowBright;
    private EditText etCheckInterval;
    private Switch switchRememberBright;
    private FloatingActionButton btnSave;

    //Preferências
    private long verifyInterval;
    private int brightnessMode;
    private boolean rememberBrightness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        initComponents();

        managePreferences = new ManagePreferences(this);

        loadPreferences();

        updateComponents();

    }

    @Override
    protected void onPause() {
        super.onPause();
        //Salvar mudanças
        saveChanges();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Salvar mudanças
        saveChanges();
    }

    /**
     * Inicializa os componentes da tela
     */
    private void initComponents(){

        rbHighLowBright = (RadioButton) findViewById(R.id.rbHighLow);
        rbLowHighBright = (RadioButton) findViewById(R.id.rbLowHigh);
        etCheckInterval = (EditText) findViewById(R.id.editTextInterval);
        switchRememberBright = (Switch) findViewById(R.id.switchRemeberBright);
        btnSave = (FloatingActionButton) findViewById(R.id.btnSave);

        btnSave.setOnClickListener(this);

    }

    /**
     * Carrega as preferências para a activity
     */
    private void loadPreferences(){

        verifyInterval = managePreferences.getLong(ManagePreferences.PREFS_CHECK_INTERVAL);
        rememberBrightness = managePreferences.getBoolean(ManagePreferences.PREFS_BACKUP_BRIGHTNESS_LEVEL);
        brightnessMode = managePreferences.getInt(ManagePreferences.PREFS_BRIGHTNESS_MODE);

    }

    /**
     * Atualiza os componentes da tela com os valores das preferências
     */
    private void updateComponents(){

        etCheckInterval.setText(String.valueOf(verifyInterval));

        switchRememberBright.setChecked(rememberBrightness);

        if(brightnessMode == ManagePreferences.VAL_BRIGHTNESS_MODE_LOW_HIGH){
            rbLowHighBright.setChecked(true);
        }
        else if(brightnessMode == ManagePreferences.VAL_BRIGHTNESS_MODE_HIGH_LOW){
            rbHighLowBright.setChecked(true);
        }

    }

    private void saveChanges(){

        verifyInterval = Long.parseLong(etCheckInterval.getText().toString());
        rememberBrightness = switchRememberBright.isChecked();

        if(rbLowHighBright.isChecked()){
            brightnessMode = ManagePreferences.VAL_BRIGHTNESS_MODE_LOW_HIGH;
        }
        else if(rbHighLowBright.isChecked()){
            brightnessMode = ManagePreferences.VAL_BRIGHTNESS_MODE_HIGH_LOW;
        }

        managePreferences.writeLong(ManagePreferences.PREFS_CHECK_INTERVAL, verifyInterval);
        managePreferences.writeBoolean(ManagePreferences.PREFS_BACKUP_BRIGHTNESS_LEVEL, rememberBrightness);
        managePreferences.writeInt(ManagePreferences.PREFS_BRIGHTNESS_MODE, brightnessMode);

        Toaster.showToast("Mudanças salvas com sucesso.", this);

        updateSettings();

    }

    private void updateSettings(){
        WatcherThread thread = WatcherThread.getInstance(getBaseContext());

        thread.updatePreferences();
    }

    @Override
    public void onClick(View view) {

        if(btnSave.equals(view)){

            //Salva as preferências
            saveChanges();
            finish();

        }

    }
}
