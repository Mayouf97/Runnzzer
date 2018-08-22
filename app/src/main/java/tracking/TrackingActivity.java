package tracking;

import android.annotation.SuppressLint;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.zgr.runnzzer.*;
import sqlitedataBase.DataBaseHelper;

import java.util.ArrayList;

//Google Map Api Classes & Packages


public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnCameraMoveStartedListener,View.OnLongClickListener {

    public static final int MAP_CAMERA_ZOOM_LEVEL = 16;//Map Camera Zoom Level.
    private static final float ALPHA_VALUE = 0.6f;//Alpha Value for the Layouts after Locking the Screen.
    private static final int TIME_POINTER = 15 * 1000;// 20 Seconds.
    private GoogleMap mMap;//Google Map Member That Represent Map Fragment In User Interface.
    private ImageView layer_Icon , currentPlaceIcon;//
    private ImageButton pauseButton,lockButton,unlockButton;//Pause and Lock And unLock Buttons.
    private boolean switcher;//boolean allows as to switch from Pause Mood to Resume Mood.
    private TrackingService trackingService;//Object Allows as to Use and Access to Service Functionality.
    private IntentFilter filters;//Intent Filter For BroadCast Receiver.
    private LatLng currentLatLng;//Member Represent Current Place In Map (initialization of it when we Get Update From Service Via Intent)
    private BroadcastReceiver broadcastReceiver = new receiverClass();//BroadCast Receivers Custom Class.
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TrackingService.MyLocalBinder myLocalBinder = (TrackingService.MyLocalBinder) service;
            //Get Tracking Service Object
            trackingService = myLocalBinder.getService();
            /*
            When Client Bound to the Service
            1 get Based time use -->> getBaseTime()
            2 set  Base time
            3 start Chronometer again
            */
            if(trackingService != null){
                /* check if the client connected to Location Service or not */
                if(trackingService.isConnected()){
                    /* start timer if it is Selected On Display Settings */
                    displayCustomizer.startTimer(trackingService.getBaseTime());
                    switcher = false;
                }else {
                    /*This Happen in this Case when user clicked Pause Button
                    and Kill the Activity and returns back to the App again
                    * */
                    /* Show Pause Button to The User */
                    pauseButton.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                    /* Show Stop Button */
                    lockButton.setImageResource(R.drawable.ic_stop_white_48dp);
                    lockButton.setBackground(getResources().getDrawable(R.drawable.red_circle_button));
                    switcher = true;
                    /* display  Give as The Last Text of time */
                    displayCustomizer.setText(trackingService.getLastTextOfChronometer());
                }
                /*Lock the Screen if was in Locked Mood Before !
                Service Tell As If the User Was Lock Screen or not before he Quite the Activity*/
                if (trackingService.isScreenLocked()){
                    screenLock();
                }else {
                    unLockScreen();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private SettingManager settingManager;//Allows as To Get and Change App Settings.
    private DisplayCustomizer displayCustomizer;//Object That Allow as to Change Displays.
    private ArrayList<TextView> titles = new ArrayList<>();//Array To Hold The TextViews That Represent as Titles  (Duration , Speed , Distance , elevation ...)
    private ArrayList<TextView> values = new ArrayList<>();//Array To Hold The TextViews That Show Current Value like (1.25 km , 11 km/h ...)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set the Content of the Activity !
        setContentView(R.layout.activity_live_tracking);
        //ads banner
        AdView mAdView = findViewById(R.id.tracking_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        //Get Support Map Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
        //Get Shared Preferences
        SharedPreferences sharedPreferences = getSharedPreferences(SettingManager.SETTINGS_FILE_NAME, MODE_PRIVATE);
        //initialization Of Settings Manager Object (See "SettingManager" Class Docs For More Details About Functionality Of It !)
        settingManager = new SettingManager(sharedPreferences);
        //set up the Ui
        setUpTheUserInterface();
        //Intent Filter For BroadCast Receiver
        filters = new IntentFilter(TrackingService.INTENT_ACTION_NAME);
        //Display Customizer
        displayCustomizer = new DisplayCustomizer(sharedPreferences , getTextsTitlesReference() , getTextsValuesReference() );
    }


    //find all the views and set the listener for some of them
    private void setUpTheUserInterface (){
        //Pause and resume Image Button
        pauseButton =  findViewById(R.id.pause_resume_button);
        //lock and Stop Button
        lockButton = findViewById(R.id.lock_stop_button);
        lockButton.setOnLongClickListener(this);
        //find unlock button & set visibility for it !
        unlockButton = findViewById(R.id.unlock_button);
        unlockButton.setVisibility(View.INVISIBLE);
        //Track Tracking Image
        layer_Icon = findViewById(R.id.layer_icon);
        //Current Place Image
        currentPlaceIcon = findViewById(R.id.current_place_icon);
        //Set Up Click Listener for the Ui Layout that Show User Current Data !
        findViewById(R.id.first_display_layout).setOnLongClickListener(this);
        findViewById(R.id.second_display_layout).setOnLongClickListener(this);
        findViewById(R.id.third_display_layout).setOnLongClickListener(this);
    }

    //
    private ArrayList<TextView> getTextsTitlesReference(){
        //Add Texts to
        titles.add(findViewById(R.id.live_tracking_first_title));
        titles.add(findViewById(R.id.live_tracking_second_title));
        titles.add(findViewById(R.id.live_tracking_third_title));
        return titles;
    }

    //
    private ArrayList<TextView> getTextsValuesReference(){
        //Add Texts to
        values.add(findViewById(R.id.first_value));
        values.add(findViewById(R.id.second_value));
        values.add(findViewById(R.id.third_value));
        return values;
    }


    @Override
    protected void onResume() {
        super.onResume();
        //Bound Client to Service
        bindService(new Intent(this , TrackingService.class), serviceConnection , Context.BIND_AUTO_CREATE);
        //Register the Receiver to Updates From Tracking Service    
        registerReceiver(broadcastReceiver , filters);
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(mMap != null){
            //Set Map Style Depends on User Settings
            switch (settingManager.getMapSettings()){

                //set custom style (See Raw Folder map_style.json file)
                case SettingManager.DEFAULT_MAP :
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
                    break;

                //set satellite style
                case SettingManager.SATELLITE_MAP :
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    break;

                //set Terrain style
                case SettingManager.TERRAIN_MAP :
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    break;
            }
            //marker
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.setOnCameraMoveStartedListener(this);
        }
    }


    private void getUpdate(LatLng latlng) {
        //Move Camera to the Current Place of the User
        if (mMap != null){
            //animate camera to the current place
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng , MAP_CAMERA_ZOOM_LEVEL));
        }
    }


    //draw path in the map
    private void pathDrawer (Track track){
        if(mMap != null){
            try{
                //Get The ArrayList
                ArrayList<PolylineOptions> pathList = track.getPathArrayList();
                //Check if the ArrayList is Not Empty (No Data on it)
                if (!pathList.isEmpty()){
                    //draw the path in map using addPolyline
                    for(PolylineOptions path : pathList){
                        mMap.addPolyline(path);
                    }
                }
            }catch (NullPointerException e){
                Log.i("Tag" , ""+e.getMessage());
            }
        }
    }


    //Click Event Handler
    public void onClick(View view) {
        switch (view.getId()){
            //Pause and Resume Button !
            case R.id.pause_resume_button :
                controlTrack ();
                break;
            //
            case R.id.lock_stop_button :
                if (switcher){
                    //
                    Toast.makeText(this, "Long Click To Finish", Toast.LENGTH_SHORT).show();
                }else {
                    //lock Screen
                    screenLock();
                }
                break;

            //
            case R.id.layout_layer_icon :
                layer_Icon.setColorFilter(getResources().getColor(R.color.colorAccent));
                //Custom style to the Pop Up Menu
                ContextThemeWrapper wrapper = new ContextThemeWrapper(this , R.style.PopUpStyle);
                PopupMenu popup = new PopupMenu(wrapper , view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.layer_menu, popup.getMenu());
                popup.show();
                popup.setOnDismissListener(menu -> layer_Icon.setColorFilter(getResources().getColor(R.color.white)));
                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()){
                        case R.id.satellite_map_view :
                            if(mMap != null){
                                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                            }
                            settingManager.changeMapSettings(SettingManager.SATELLITE_MAP);
                            break;

                        case R.id.night_map_view :
                            if(mMap != null){
                                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getApplicationContext() , R.raw.map_style));
                            }
                            settingManager.changeMapSettings(SettingManager.DEFAULT_MAP);
                            break;

                        case R.id.terrain_map_view :
                            if(mMap != null){
                                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                            }
                            settingManager.changeMapSettings(SettingManager.TERRAIN_MAP);
                            break;
                    }
                    return true;
                });
                break;

            //
            case R.id.layout_current_place_icon :
                //move Camera to the Current Place if the LatLng Not null
                if (currentLatLng != null && mMap != null){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng , MAP_CAMERA_ZOOM_LEVEL));
                    currentPlaceIcon.setColorFilter(getResources().getColor(R.color.colorAccent));
                }else {
                    //explain
                    Toast.makeText(this, "We Don't Get Location Updates Yet !", Toast.LENGTH_SHORT).show();
                }
                break;
            //UNLOCK SCREEN
            case R.id.unlock_button :
                Toast.makeText(TrackingActivity.this, "Long Click to Unlock The Screen ", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    //Called when User Clicked Pause or Resume Buttons
    private void controlTrack (){
            //it will execute When User Click Pause Button
            if (!switcher){
                //Modifies the Pause Button
                pauseButton.setImageResource(R.drawable.ic_play_arrow_white_48dp);
                //lock Button
                lockButton.setImageResource(R.drawable.ic_stop_white_48dp);
                lockButton.setBackground(getResources().getDrawable(R.drawable.red_circle_button));
                switcher = true;
                //Pause Location Service
                if(trackingService != null){
                    //Pause the Service by calling the pause Method and Give the Service the Last Text to Use it
                    //in case the User Kill the Activity and Come Back Again !
                    //See The ServiceConnection Class Down 180 Line to See Exact Details
                    if (displayCustomizer.getText() != null ){
                        trackingService.pause((String) displayCustomizer.getText());
                    }else {
                        trackingService.pause("00:00");
                    }
                    displayCustomizer.stopTimer();
                }
            }
            //execute When User Click Resume Button !
            else {
                //Modifies the Pause Button
                pauseButton.setImageResource(R.drawable.ic_pause_white_48dp);
                //lock Button
                lockButton.setImageResource(R.drawable.ic_lock_white_36dp);
                lockButton.setBackground(getResources().getDrawable(R.drawable.black_circle_button));
                switcher = false;
                //Location reConnected Again
                //Here is the problem
                if(trackingService != null){
                    trackingService.resume();
                    displayCustomizer.startTimer(trackingService.getBaseTime());
                }
            }
    }



    @Override
    public void onCameraMoveStarted (int i) {
        currentPlaceIcon.setColorFilter(getResources().getColor(R.color.white));
    }


    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()){

            case R.id.lock_stop_button :
                //finish Tracking
                if (switcher) {
                    //finish activity_history
                    finishSession();
                }
                break;

            case R.id.first_display_layout :
                //
                displayCustomizer.editFirstDisplayPanel(this , createAlertBox());
                break;

            case R.id.second_display_layout :
                //
                displayCustomizer.editSecondsDisplayPanel(this , createAlertBox());
                break;

            case R.id.third_display_layout :
                //
                displayCustomizer.editThirdDisplayPanel(this , createAlertBox());
                break;
            //
            case R.id.unlock_button :
                unLockScreen();
                break;
        }
        return true;
    }


    //create a dialog box for changing Display
    private View createAlertBox (){
        //Get screen displays
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        //Get the dimensions of the screen from the metrics
        int heightPixels = metrics.heightPixels;
        int widthPixels = metrics.widthPixels;
        //Inflate the layout
        View alertDialogView = getLayoutInflater().inflate( R.layout.select_display, null);
        //set sizes for the view
        alertDialogView.setMinimumWidth(widthPixels);
        alertDialogView.setMinimumHeight((heightPixels*2) / 3);
        return alertDialogView;
    }



    //Lock Screen
    private void screenLock (){
        //Change Mood in Service
        trackingService.changeLockMood(true);
        if(mMap != null){
            mMap.getUiSettings().setZoomControlsEnabled(false);
            mMap.getUiSettings().setAllGesturesEnabled(false);
        }
        pauseButton.setVisibility(View.INVISIBLE);
        lockButton.setVisibility(View.INVISIBLE);
        unlockButton.setVisibility(View.VISIBLE);
        unlockButton.setOnLongClickListener(this);
        //layouts
        View layerLayout = findViewById(R.id.layout_layer_icon);
        View placeLayout = findViewById(R.id.layout_current_place_icon);
        //panels
        View firstPanel = findViewById(R.id.first_display_layout);
        View secondPanel = findViewById(R.id.second_display_layout);
        View thirdPanel = findViewById(R.id.third_display_layout);
        //Disable Click and Long Clicks
        firstPanel.setLongClickable(false);
        secondPanel.setLongClickable(false);
        thirdPanel.setLongClickable(false);
        layerLayout.setClickable(false);
        placeLayout.setClickable(false);
        //Change Opacity
        layerLayout.setAlpha(ALPHA_VALUE);
        placeLayout.setAlpha(ALPHA_VALUE);
        firstPanel.setAlpha(ALPHA_VALUE);
        secondPanel.setAlpha(ALPHA_VALUE);
        thirdPanel.setAlpha(ALPHA_VALUE);
    }



    //Unlock The Screen
    private void unLockScreen (){
        //
        trackingService.changeLockMood(false);
        if(mMap != null){
            //Map Settings
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setAllGesturesEnabled(true);
        }
        pauseButton.setVisibility(View.VISIBLE);
        lockButton.setVisibility(View.VISIBLE);
        unlockButton.setVisibility(View.INVISIBLE);
        //layouts
        View layerLayout = findViewById(R.id.layout_layer_icon);
        View placeLayout = findViewById(R.id.layout_current_place_icon);
        //panels
        View firstPanel = findViewById(R.id.first_display_layout);
        View secondPanel = findViewById(R.id.second_display_layout);
        View thirdPanel = findViewById(R.id.third_display_layout);
        //Disable Click and Long Clicks
        firstPanel.setLongClickable(true);
        secondPanel.setLongClickable(true);
        thirdPanel.setLongClickable(true);
        layerLayout.setClickable(true);
        placeLayout.setClickable(true);
        //Change Opacity
        layerLayout.setAlpha(1f);
        placeLayout.setAlpha(1f);
        firstPanel.setAlpha(1f);
        secondPanel.setAlpha(1f);
        thirdPanel.setAlpha(1f);
    }



    private class receiverClass extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Update Ui
            displayCustomizer.UiUpdater(TrackingActivity.this , intent);
            //Convert String to LatLng Object wow Using Gson
            assert intent.getExtras() != null;
            LatLng latlng = Converter.convertStringToLatLng(intent.getExtras().getString(Keys.LAT_LNG));
            currentLatLng = latlng;
            //Update the Map
            getUpdate(latlng);
            //Path
            String pathAsString = intent.getExtras().getString(Keys.PATH);
            Track track = Converter.convertStringToSessionPath(pathAsString);
            pathDrawer(track);
        }
    }



    //This called when user click finish button
    private void finishSession (){
        //Get ActivityInformation Object ready to
        ActivityInformation activityInformation;
        double totalTime;

        //Time As String format
        //Calculate The Total Time of the Session
        if (switcher){
            long newBaseTime = trackingService.getBaseTime() + (SystemClock.elapsedRealtime() - trackingService.getPauseSystemTime());
            totalTime = SystemClock.elapsedRealtime() - newBaseTime ;
        }
        else {
            totalTime = SystemClock.elapsedRealtime() - trackingService.getBaseTime();
        }

        //unbind from service
        unbindService(serviceConnection);

        //Kill this Activity
        finish();

        //Check the Duration of tracking if it less than TIME_POINTER value
        //it's not necessary to save the tracking session
        if (totalTime < TIME_POINTER){
            //Tell the Service to stop tracking and save Data to the DataBase
            trackingService.workoutCanceled();
            //Start MainActivity Activity
            startActivity(new Intent(this , MainActivity.class));
            //
            Transitions.closeTransition(this);

        }else {
            //Get Object That Hold And represent Session
            activityInformation = trackingService.saveAllData(totalTime);//this method set up activityInformation Object and stop service
            //Save Data in DataBase
            DataBaseHelper dataBaseHelper = new DataBaseHelper(this);//database Helper
            dataBaseHelper.saveActivityData(activityInformation);
            //Create Intent to Open Activity and hold some data
            Intent i = new Intent(TrackingActivity.this, TrackingDetails.class);
            //Put Data to Identify the Activity! Cause(TrackingDetails Activity Can Open By Two Activities this One And History)
            i.putExtra(TrackingDetails.WHO, TrackingDetails.MAP_OPENED_ME);
            //Start New Activity
            startActivity(i);
            //
            Transitions.openTransition(this);
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        try {
            //Try Un bind From the Service !
            unbindService(serviceConnection);
        }catch (Exception ignored){}
        //Un register BroadCast Receiver
        unregisterReceiver(broadcastReceiver);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
