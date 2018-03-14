package com.singh.multimeet.quicxplo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.singh.multimeet.quicxplo.Util;

/**
 * Created by multimeet on 10/2/18.
 */

public class BootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

            Log.e(BootUpReceiver.class.getSimpleName(),"Boot completed");
            Toast.makeText(context.getApplicationContext(),"Boot completed!!!",Toast.LENGTH_LONG).show();
            SharedPreferences sharedPreferences=context.getSharedPreferences(Util.DIR_DATA,Context.MODE_PRIVATE);
            sharedPreferences.edit().putString(Util.BASE_URI,"").apply();


    }
}
