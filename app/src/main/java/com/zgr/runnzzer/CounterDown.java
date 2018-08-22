package com.zgr.runnzzer;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import tracking.TrackingActivity;
import tracking.TrackingService;


public class CounterDown extends AppCompatActivity {


    private long futureTime;
    private TextView downTimeText;
    private Handler handler = new Handler();
    private Runnable runnable;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter_down);
        downTimeText = findViewById(R.id.time_down_timer);
        futureTime = SystemClock.elapsedRealtime() + (11 * 1000);
        //start counting down
        startDownCounter();
    }




    //start down count
    private void startDownCounter (){
        runnable = () -> {
            //Calculate rest Time in ms
            long downTimerValue = futureTime - SystemClock.elapsedRealtime();
            //Convert to seconds
            int sec = (int) downTimerValue / 1000;
            //set Text For the Timer
            downTimeText.setText(String.valueOf(sec));
            //when Time equal to Zero we Direct User to The Live Tracking Activity
            if (sec == 0){
                startTrackingActivity();
            }
            //wait one second
            handler.postDelayed(runnable, 1000);
        };
        //start exe runnable code
        handler.postDelayed(runnable, 0);
    }




    //start recording by starting TrackingActivity and Tracking Service
    private void startTrackingActivity(){
        //remove callback
        handler.removeCallbacks(runnable);
        //finish the current activity
        finish();
        //start Tracking Service
        startService(new Intent(this, TrackingService.class));
        //start map activity
        startActivity(new Intent(this, TrackingActivity.class));
        //
        Transitions.openTransition(this);
    }




    //Event Handler From The Ui
    public void onClick (View view){
        startTrackingActivity();
    }




}
