package com.zgr.runnzzer;


import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import sqlitedataBase.DataBaseHelper;
import tracking.TrackingActivity;
import tracking.TrackingService;

import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST_CODE = 0;//Request Codes for location permission
    private DrawerLayout drawerLayout;//drawer Layout


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check for the "Running Services" is Running !
        // in Case the Service is Active we direct the user to the Map Activity and kill this activity
        if (isServiceRunning()) {
            //Close the Activity
            finish();
            //Start the Map Activity
            startActivity(new Intent(this, TrackingActivity.class));
        }else {
            //set the content view of the activity
            setContentView(R.layout.activity_main);

            //ads bannertracking_ad
            AdView mAdView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);

            //Drawer layout
            drawerLayout = findViewById(R.id.drawerLayout);
            //Navigation view
            NavigationView navigationView =  findViewById(R.id.navigationView);
            //set up the listener for the clicks events
            navigationView.setNavigationItemSelectedListener(item -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                item.setChecked(true);
                //
                switch (item.getItemId()){
                    case R.id.nav_history :
                        //
                        startActivity(new Intent(MainActivity.this , History.class));
                        break;
                    case R.id.nav_setting :
                            //
                        startActivity(new Intent(MainActivity.this , SettingsActivity.class));
                        break;
                        case R.id.nav_profile :
                            //
                            startActivity(new Intent(MainActivity.this , UserProfile.class));
                            break;
                    }
                    Transitions.openTransition(this);
                    return false;
                });
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        //update the Ui
        UiUpdater();
    }



    //Update The User Interface
    private void UiUpdater() {
        //Access DataBase
        DataBaseHelper dataBase = new DataBaseHelper(this);
        //Find Text Views
        TextView totalDistance = findViewById(R.id.TotalDistance);
        TextView unite = findViewById(R.id.unite);
        TextView totalRun = findViewById(R.id.runs);
        TextView time = findViewById(R.id.time);
        //Total Time
        time.setText(new Converter().calculateTotalTime(this));

        //Total Runs
        totalRun.setText(String.format(Locale.ENGLISH , "%02d" ,dataBase.readAllData().size()));

        //total Distance
        totalDistance.setText(new Converter().calculateTotalDistance(this, getSharedPreferences(SettingManager.SETTINGS_FILE_NAME, MODE_PRIVATE)));

        //Current Unite in Settings
        if (new SettingManager(getSharedPreferences(SettingManager.SETTINGS_FILE_NAME, MODE_PRIVATE)).getDistanceUniteSettings() == SettingManager.DEFAULT_UNIT_VALUE) {
            unite.setText(getResources().getString(R.string.km));
        } else {
            unite.setText(getResources().getString(R.string.mi));
        }

    }



    @Override
    public void onBackPressed() {
        //check for the navigation drawer is open or not
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            //close the drawer
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            //drawer closed so close the app can perform back action
            //close the Activity
            super.onBackPressed();
        }
    }



    //Click Events Handler
    public void onClick(View view) {
        switch (view.getId()) {

            //start button
            case R.id.startButton:
                //
                askLocationPermission();
                checkConditionsToStartCounterDownActivity();
                break;

            //music icon
            case R.id.music_icon:
                Intent intent = new Intent("android.intent.action.MUSIC_PLAYER");
                //Open Music App Safely
                ComponentName componentName = intent.resolveActivity(getPackageManager());
                if (componentName != null) {
                    startActivity(intent);
                    Transitions.openTransition(this);
                } else {
                    //explain to user why we could'nt Open Music Application !
                    Toast.makeText(this, "Problem Happened Try To Open Music App Manually ", Toast.LENGTH_SHORT).show();
                }
                break;

            // drawer icon
            case R.id.drawer_menu :
                //Open Drawer
                drawerLayout.openDrawer(GravityCompat.START);
                break;
        }
    }



    private void askLocationPermission() {
        //check android version
        if (Build.VERSION.SDK_INT >= 23){
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                //Permission Denied
                //ask user about permissions
                requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION} , LOCATION_REQUEST_CODE);
            }else {
                //Permission Granted
                checkConditionsToStartCounterDownActivity();
            }
        }else {
            //Under version 23
            checkConditionsToStartCounterDownActivity();
        }
    }


    /*This Method check if the "Tracking" service is Running or not !
    *if service running We direct the user to the Tracking Activity
    *if the services is'nt currently running we direct him to the MainActivity Activity*/
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        //Get the list of the Running services by Using the getRunning services from Activity Manager
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                //Check for the Service via Name "TrackingService.class"
                if (TrackingService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }



    //Check if the Battery Saver is activated or not
    private boolean checkForBatterySaver() {
        PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        //This Option Available only for devices That Running Android version 21
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return powerManager != null && powerManager.isPowerSaveMode();
        }
        return false;
    }



    //Return to Us the State of the location Service (activated or not)
    private boolean LocationServicesState() {
        //Get Location Manager
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager != null){
            //Provider List
            String[] providersList = {
                    LocationManager.GPS_PROVIDER ,
                    LocationManager.NETWORK_PROVIDER
            };
            for (String aProvidersList : providersList) {
                if (manager.isProviderEnabled(aProvidersList)) {
                    return true;
                }
            }
        }
        return false;
    }


    //CallBack Method get Called when User Interact with permission Dialog
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST_CODE :
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission Granted
                    Toast.makeText(this, "Perfect Permission Granted !", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "This App Need Location Permission from you to detect your location while tracking !", Toast.LENGTH_LONG).show();
                }

                break;
        }

    }


    //start TrackingActivity and  start TrackingService
    private void checkConditionsToStartCounterDownActivity() {
        //Check for The Gps is Activated or not
        if (LocationServicesState()) {
            //check if the Power Saver is activated or not !
            if (!checkForBatterySaver()) {
                startCounterDownActivity();
            } else {
                //changeDistanceUnite this with alert dialog is better
                batterySaverAlertDialog();
            }
        } else {
            //when the gps is disable show a dialog
            gpsDisabledAlertDialog();
        }
    }


    private void startCounterDownActivity (){
        //kill current activity
        finish();
        //start down counter activity
        startActivity(new Intent(this , CounterDown.class));
        //
        Transitions.openTransition(this);
    }


    //Show Alert Dialog when location Option don't active in Device!
    private void gpsDisabledAlertDialog () {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        //inflate layout
        LayoutInflater inflater = getLayoutInflater();
        //inflate resources
        View view = inflater.inflate(R.layout.custom_dialog_for_gps, null);
        //set Content View for the Dialog
        alertDialog.setView(view);
        //find enable button
        Button enableButton = view.findViewById(R.id.enable_button);
        //set click listener for the enable button
        enableButton.setOnClickListener(view1 -> {
            //create intent
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            //Open Active Gps Setting Activity Safely Using resolver
             if (intent.resolveActivity(getPackageManager()) != null){
                 //start activity
                startActivity(intent);
                //start the activity with transition
                 Transitions.openTransition(this);
            }else {
            //Explaining Toast to the User !
                Toast.makeText(MainActivity.this, "Error Happens ! You Can Active Your GPS Manually", Toast.LENGTH_LONG).show();
            }
            //Close the Alert Box
            alertDialog.dismiss();
        });
        //find continue button
        Button continueButton = view.findViewById(R.id.continue_button);
        //set click listener for continue button
        continueButton.setOnClickListener(view12 -> {
            //kill current activity
            finish();
            //start Tracking service
            startCounterDownActivity ();
            //Close the Alert Box
            alertDialog.dismiss();
        });
        //show the alert dialog for the user
        alertDialog.show();
    }



    /*show an  alert Dialog when battery Saver is activated on the device
    * to make the user aware about the effect of the "batterySaver" to the Location accuracy */
    private void batterySaverAlertDialog (){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        //inflate layout to be the view for the alert Dialog
        View view = getLayoutInflater().inflate(R.layout.battery_saver_alert_box, null);
        //add the view to the alert dialog from the resources
        alertDialog.setView(view);
        //set Up Click Listener for the Button in The Alert Dialog
        view.findViewById(R.id.continue_any_way).setOnClickListener(view1 -> {
            //
            startCounterDownActivity();
            //close the alert Dialog
            alertDialog.dismiss();
        });
        //show the alert Dialog
        alertDialog.show();
    }


}
