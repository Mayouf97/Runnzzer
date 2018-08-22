package com.zgr.runnzzer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;

public class SplashScreen extends AppCompatActivity {

    public static final int time_to_wait = 3 * 1000;
    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splach_screen);
        //initialize 'admob' services
        MobileAds.initialize(this, getResources().getString(R.string.admob_key));
    }


    @Override
    protected void onResume() {
        super.onResume();
        //installing the Runnable That Execute the Code after waiting few seconds (time_to_wait)
        runnable = () -> {
            //finish the activity
            finish();
            //start the home activity
            startActivity(new Intent(SplashScreen.this , MainActivity.class));
            //
            Transitions.openTransition(this);
            //remove callback from the Handler
            handler.removeCallbacks(runnable);
        };
        //After the Screen shows wait 5 second and switch to the home activity
        handler.postDelayed(runnable , time_to_wait);
    }


}
