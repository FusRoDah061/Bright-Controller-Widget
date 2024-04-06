package com.brightcontroller.allex.brightcontroller;

import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

public class PreferencesActivity extends AppCompatActivity implements View.OnClickListener {

    private ManagePreferences managePreferences;

    private RadioButton rbLowHighBright;
    private RadioButton rbHighLowBright;
    private EditText etCheckInterval;
    private TextView rememberBrightLabel;
    private Switch switchRememberBright;
    private TextView screenOffLabel;
    private Switch switchRunScreenOff;
    private FloatingActionButton btnSave;

    //Preferências
    private long verifyInterval;
    private int brightnessMode;
    private boolean rememberBrightness;
    private boolean screenOff;

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
        screenOffLabel = (TextView) findViewById(R.id.screenOffLabel);
        switchRememberBright = (Switch) findViewById(R.id.switchRemeberBright);
        switchRunScreenOff = (Switch) findViewById(R.id.switchScreenOff);
        rememberBrightLabel = (TextView) findViewById(R.id.rememberBrightLabel);
        btnSave = (FloatingActionButton) findViewById(R.id.btnSave);

        btnSave.setOnClickListener(this);

        screenOffLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switchRunScreenOff.isChecked()){
                    switchRunScreenOff.setChecked(false);
                }
                else{
                    switchRunScreenOff.setChecked(true);
                }
            }
        });

        rememberBrightLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switchRememberBright.isChecked()){
                    switchRememberBright.setChecked(false);
                }
                else{
                    switchRememberBright.setChecked(true);
                }
            }
        });

    }

    /**
     * Carrega as preferências para a activity
     */
    private void loadPreferences(){

        verifyInterval = managePreferences.getInterval();
        rememberBrightness = managePreferences.getRememberBrightness();
        brightnessMode = managePreferences.getBrightnessMode();
        screenOff = managePreferences.getRunOnScreenOff();
    }

    /**
     * Atualiza os componentes da tela com os valores das preferências
     */
    private void updateComponents(){

        etCheckInterval.setText(String.valueOf(verifyInterval / 1000));

        switchRememberBright.setChecked(rememberBrightness);

        switchRunScreenOff.setChecked(screenOff);

        if(brightnessMode == ManagePreferences.VAL_BRIGHTNESS_MODE_LOW_HIGH){
            rbLowHighBright.setChecked(true);
        }
        else if(brightnessMode == ManagePreferences.VAL_BRIGHTNESS_MODE_HIGH_LOW){
            rbHighLowBright.setChecked(true);
        }

    }

    private void saveChanges(){

        verifyInterval = Long.parseLong(etCheckInterval.getText().toString()) * 1000;
        rememberBrightness = switchRememberBright.isChecked();
        screenOff = switchRunScreenOff.isChecked();

        if(rbLowHighBright.isChecked()){
            brightnessMode = ManagePreferences.VAL_BRIGHTNESS_MODE_LOW_HIGH;
        }
        else if(rbHighLowBright.isChecked()){
            brightnessMode = ManagePreferences.VAL_BRIGHTNESS_MODE_HIGH_LOW;
        }

        managePreferences.setInterval(verifyInterval);
        managePreferences.setRememberBrightness(rememberBrightness);
        managePreferences.setBrightnessMode(brightnessMode);
        managePreferences.setRunOnScreenOff(screenOff);

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
