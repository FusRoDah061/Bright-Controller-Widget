package com.brightcontroller.allex.brightcontroller;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * Created by allex on 24/06/17.
 */

public class Toaster {

    public static void showToast(String message, Context context){

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

    }

    public static void TshowToast(String message, Context context){
        Handler h = new Handler(Looper.getMainLooper());
        final String msg = message;
        final Context con = context;


        h.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(con, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
