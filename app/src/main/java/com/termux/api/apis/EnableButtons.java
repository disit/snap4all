package com.termux.api.apis;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.JsonWriter;
import android.util.Log;

import com.termux.MainActivity;
import com.termux.api.TermuxApiReceiver;
import com.termux.api.util.ResultReturner;

// TERMUX MERGE
// This is the implementation of the termux-api command "termux-enable-buttons". Its purpose is to forward the call to the MainActivity with an appropriate delay.
public class EnableButtons {
    public static void onReceive(TermuxApiReceiver apiReceiver, final Context context, final Intent intent) {
        ResultReturner.returnData(apiReceiver, intent, new ResultReturner.ResultJsonWriter() {

            final Handler handler = new Handler();

            @Override
            public void writeJson(final JsonWriter out) {
                try{
                    Log.d("termux-api-java", "Setting enableNodeRed to true");
                    MainActivity.enableNodeRed=true;

                    if(MainActivity.activity!=null) {
                        handler.postDelayed(() -> {
                            Log.d("termux-api-java", "Calling enableButtons");
                            MainActivity.activity.enableButtons();
                        }, 5000);
                    }
                }catch(Exception e){
                    Log.d("termux-api-java","EnableButtons failed with:\n"+e.getMessage());
            }}
        });
    }
}
