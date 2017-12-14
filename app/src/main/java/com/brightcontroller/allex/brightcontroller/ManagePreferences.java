package com.brightcontroller.allex.brightcontroller;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by allex on 25/06/17.
 *
 * Classe que contém as operações de escrita e leitura das configurações do aplicativo.
 * É aqui que as preferências do usuário são modificadas e salvas.
 */

public class ManagePreferences {

    //Chaves das preferências
    /**
     * [String]
     * Constante com o nome do arquivo de preferências do usuário
     */
    public static final String PREFS_NAME = "com.brightcontroller.allex.USER_SETTINGS";

    /**
     * [String]
     * Constante com a chave que indica o modo como o brilho da tela é ajustado.
     * Os valores possíveis são:
     * VAL_BRIGHTNESS_MODE_HIGH_LOW: Aumenta o brilho conforme a luminosidade diminui
     * VAL_BRIGHTNESS_MODE_LOW_HIGH: Aumenta o brilho conforme a luminosidade aumenta (padrão)
     */
    public static final String PREFS_BRIGHTNESS_MODE = "com.brightcontroller.allex.BRIGHTNESS_MODE";

    /**
     * [String]
     * Constante com a chave quue indica o intervalo com que o serviço deve verificar a luminosidade.
     * O valor padrão é 5000 milisegundos (5 segundos).
     * O valor é representado como long
     */
    public static final String PREFS_CHECK_INTERVAL = "com.brightcontroller.allex.CHECK_INTERVAL";

    private final String PREFS_EXISTS = "com.brightcontroller.allex.EXISTS";

    public static final String PREFS_IS_RUNNING = "com.brightcontroller.allex.RUNNING";

    /**
     * [String]
     * Constante com a chave que indica se deve ou não lembrar o nível de brilho da tela antes do widget ser adicionado.
     * Valor padrão é VAL_REMEMBER_BRIGHTNESS_LEVEL
     */
    public static final String PREFS_BACKUP_BRIGHTNESS_LEVEL = "com.brightcontroller.allex.REMEMBER_BRIGHTNESS";

    //Valores das preferências

    /**
     * [int]
     * Valor padrão para indicar ausencia
     */
    public static final int VAL_DEFAULT_VALUE_INT = -1;

    /**
     * [int]
     * Aumenta o brilho conforme a luminosidade diminui
     */
    public static final int VAL_BRIGHTNESS_MODE_HIGH_LOW = 0;

    /**
     * [int]
     * Aumenta o brilho conforme a luminosidade aumenta (padrão)
     */
    public static final int VAL_BRIGHTNESS_MODE_LOW_HIGH = 1;

    /**
     * [int]
     * Tempo padrão de intervalo de verificação da luminosidade
     */
    public static final int VAL_DEFAULT_UPDATE_INTERVAL = 5000;

    /**
     * [boolean]
     * Sinaliza para lembrar o nível de brilho antes do widget ser adicionado.
     */
    public static final boolean VAL_REMEMBER_BRIGHTNESS_LEVEL = true;

    private SharedPreferences sharedPreferences;

    public ManagePreferences(Context context){

        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if(!checkPreferences()) {
            setDefaultPrefs();
        }
    }

    /**
     * Restaura as preferências para seus valores padrão.
     */
    public void setDefaultPrefs(){
        writeInt(PREFS_BRIGHTNESS_MODE, VAL_BRIGHTNESS_MODE_LOW_HIGH);
        writeLong(PREFS_CHECK_INTERVAL, VAL_DEFAULT_UPDATE_INTERVAL);
        writeBoolean(PREFS_BACKUP_BRIGHTNESS_LEVEL, VAL_REMEMBER_BRIGHTNESS_LEVEL);

        writeBoolean(PREFS_EXISTS, true);
    }

    /**
     * Verifica se as preferências já foram registradas
     * @return true caso já existam as preferencias, false caso contrário
     */
    private boolean checkPreferences(){
        return getBoolean(PREFS_EXISTS);
    }

    /**
     * Escreve um valor inteiro nas preferências
     * @param key chave que será modificada
     * @param value novo valor da chave
     */
    public void writeInt(String key, int value){

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(key, value);

        editor.commit();

    }

    /**
     * Escreve um valor real nas preferências
     * @param key chave que será modificada
     * @param value novo valor da chave
     */
    public void writeFloat(String key, float value){

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putFloat(key, value);

        editor.commit();

    }

    /**
     * Escreve uma string nas preferências
     * @param key chave que será modificada
     * @param value novo valor da chave
     */
    public void writeString(String key, String value){

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(key, value);

        editor.commit();

    }

    /**
     * Escreve um valor boleano nas preferências
     * @param key chave que será modificada
     * @param value novo valor da chave
     */
    public void writeBoolean(String key, boolean value){

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(key, value);

        editor.commit();

    }

    /**
     * Escreve um valor long nas preferências
     * @param key chave que será modificada
     * @param value novo valor da chave
     */
    public void writeLong(String key, long value){

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putLong(key, value);

        editor.commit();

    }


    /**
     * Obtém um inteiro das preferências.
     * @param key chave que terá seu valor buscado.
     * @return ou valor da chave buscada ou VAL_DEFAULT_VALUE_INT caso a chave não exista ou não seja do tipo inteiro.
     */
    public int getInt(String key){

        try {

            return sharedPreferences.getInt(key, VAL_DEFAULT_VALUE_INT);

        } catch(ClassCastException ex){
            ex.printStackTrace();
            return VAL_DEFAULT_VALUE_INT;
        }

    }

    /**
     * Obtém um real das preferências.
     * @param key chave que terá seu valor buscado.
     * @return ou valor da chave buscada ou VAL_DEFAULT_VALUE_INT caso a chave não exista ou não seja do tipo float.
     */
    public float getFloat(String key){

        try{

            return sharedPreferences.getFloat(key, VAL_DEFAULT_VALUE_INT);

        } catch(ClassCastException ex){
            ex.printStackTrace();
            return VAL_DEFAULT_VALUE_INT;
        }

    }

    /**
     * Obtém uma string das preferências.
     * @param key chave que terá seu valor buscado.
     * @return ou valor da chave buscada ou null caso a chave não exista ou não seja do tipo String.
     */
    public String getString(String key){

        try{

            return sharedPreferences.getString(key, null);

        } catch(ClassCastException ex){
            ex.printStackTrace();
            return null;
        }

    }

    /**
     * Obtém um valor boleano das preferências.
     * @param key chave que terá seu valor buscado.
     * @return ou valor da chave buscada ou false caso a chave não exista ou não seja do tipo boolean.
     */
    public boolean getBoolean(String key){

        try{

            return sharedPreferences.getBoolean(key, false);

        } catch(ClassCastException ex){
            ex.printStackTrace();
            return false;
        }

    }

    /**
     * Obtém um long das preferências.
     * @param key chave que terá seu valor buscado.
     * @return ou valor da chave buscada ou VAL_DEFAULT_VALUE_INT caso a chave não exista ou não seja do tipo long.
     */
    public long getLong(String key){

        try{

            return sharedPreferences.getLong(key, VAL_DEFAULT_VALUE_INT);

        } catch(ClassCastException ex){
            ex.printStackTrace();
            return VAL_DEFAULT_VALUE_INT;
        }

    }

}
