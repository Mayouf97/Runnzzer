package tracking;


import android.annotation.SuppressLint;
import android.app.*;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.*;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import com.google.android.gms.maps.model.LatLng;
import com.zgr.runnzzer.Converter;
import com.zgr.runnzzer.Keys;
import com.zgr.runnzzer.R;
import com.zgr.runnzzer.SettingManager;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;


public class TrackingService extends Service {

    private static final String NOTIFICATION_CHANEL_ID = "id";//Notification Channel ID
    public static final int notification_id = 0;//Notification channel ID
    protected static final String INTENT_ACTION_NAME = "locationDataIntent";//Intent Action Name Can be Used By Another App Component To Receive The Intent
    private LocationManager locationManager; //Location Manager
    private locationHandler locationHandler = new locationHandler ();//class the implement Location Listener
    private static final int LOCATION_UPDATES_INTERVAL = 5000;//Location request interval time
    private final IBinder myBinder = new MyLocalBinder(); //used to Bound the Client with service
    private Location oldLocation; //This Location Object we use it to calculate the Distances btw two locations
    private double bestSpeed = 0;//Counters bestSpeed var holder
    private double disSum = 0;//Distance Sum Counter Holder
    private double highestElevationRecorded = 0;
    private int distanceUnit;//holds the Current Distance Unit of  activity_settings
    private long baseTime = SystemClock.elapsedRealtime();//current Time of system
    private long oldTime;//hold old value of time in pause and used to calculate new base time in resume
    private boolean isConnected = true;//Booleans for checking the Stat of the Client is in pause or resume mood
    private boolean isScreenLocked;//boolean to store the current state of the activity (lock & unlock)
    private String chronometerText;//Text Of the Chronometer
    private ActivityInformation activityInformation = new ActivityInformation();//ActivityInformation to Hold all the information of activity_history
    private Track track = new Track();//helper class for drawing path in map
    private Intent i = new Intent(INTENT_ACTION_NAME);//Intent To Send to The Client
    private AudioFeedBack audioFeedBack;//audio feed back player



    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        //
        showServiceNotification();
        //activity_settings Manager
        SettingManager settingManager = new SettingManager(getSharedPreferences(SettingManager.SETTINGS_FILE_NAME, MODE_PRIVATE));
        //Get current Unite Distance (Km or Mi) in user activity_settings !
        distanceUnit = settingManager.getDistanceUniteSettings();
        //start location updates using location manager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        assert locationManager != null;
        //asking location updates from Gps Provider & Network Provider
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER , LOCATION_UPDATES_INTERVAL, 0 , locationHandler);

        }else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            //
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER , LOCATION_UPDATES_INTERVAL, 0 , locationHandler);
        }
        //Audio Configuration
        audioFeedBack = new AudioFeedBack(settingManager);
    }


    private void showServiceNotification(){
        //Intent of the Map Activity
        Intent notificationIntent = new Intent(this, TrackingActivity.class);

        //For Android Oreo and Higher Versions
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //Pending Intent For the Notification
            PendingIntent pendingIntent = PendingIntent.getActivity(this , 0 , notificationIntent , PendingIntent.FLAG_NO_CREATE);

            //setup and create Notification
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANEL_ID , "Notification" , NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Tracking Your Work-out");
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);

            //Notification Builder
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANEL_ID)
                    .setSmallIcon(R.mipmap.running_icon)
                    .setContentTitle("My notification")
                    .setContentText("Hello World!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent);
            //show Notification Manager in status bar
            notificationManager.notify( notification_id , mBuilder.build());

        }else {
            //Under Android Oreo Versions
            //Pending Intent and in Here we Gonna use our Intent
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , notificationIntent, 0);
            //Notification Builder
            Notification notification = new Notification.Builder(this)
                    .setContentIntent(pendingIntent)
                    .setContentTitle("Runnzzer")
                    .setContentText("Tracking your workout")
                    .setSmallIcon(R.mipmap.running_icon)
                    .build();
            //show notification bar in foreground
            startForeground(1 , notification);
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        //it A call back Method get call when user Bound to The Service !
        return myBinder;
    }


    //class for bound the client with service
    class MyLocalBinder extends Binder {
        TrackingService getService() {
            return TrackingService.this;
        }
    }


    //this Method will be executed when User Using the KiloMeter as a Distance Unite in Settings
    private void processTheLocationInKm (Location location){
        //create LatLng  From Location That we received
        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
        //check the accuracy of the location and the provider
        if (location.getAccuracy() <= 20){
            /* When isConnected = true
            * That Mean The User Still Recording His Activity
            * So Calculate all the data and Put Them into The Intent and Send it to TrackingActivity (Client).
            *
            * When isConnected = false
            * That mean the user Stop Recording His Activity By pressing Pause Button in TrackingActivity (Client)
            * In This Case We Don't Calculate any Data (distance , elev ..)
            * But we still send To The Client The Current Position
            * And Path To TrackingActivity (Client)*/
            if(isConnected){

                //Get Speed From the Location if they have it by Using getSpeed and That return speed in meters/second
                //Convert Speed from meters to the Kilometers
                double speedKm = (location.getSpeed() / 1000) * 3600;

                //Get current Elevation
                double elevation = location.getAltitude();

                //Calculate BestSpeed
                if (speedKm > bestSpeed) {
                    //Change the Best Speed if the condition is true
                    bestSpeed = speedKm;
                }

                //Calculate The Highest Elevation
                if (elevation > highestElevationRecorded){
                    highestElevationRecorded = elevation;
                }

                //Calculate Distance
                // Collect Data if the distance increased by 0.25 km than the previous value of distance
                if (oldLocation != null) {
                    //calculate Distance
                    //Get distance using distanceTo method
                    double disMeter = location.distanceTo(oldLocation);
                    //Convert distance from meters to kilometers
                    double disKm = disMeter / 1000;
                    //Distance Sum
                    disSum = disSum + disKm;
                    //play audio feedback about the tracking
                    audioFeedBack.playAudio(baseTime , disSum);
                    //add data (speed , distance , bestSpeed , elevation) to the Intent
                    i.putExtra(Keys.SPEED, speedKm);
                    i.putExtra(Keys.DISTANCE, disSum);
                    i.putExtra(Keys.BEST_SPEED , bestSpeed);
                    i.putExtra(Keys.ELEVATION , elevation);
                }
            }
            //prepared Path Data To construct the the track
            preparePathData(location , oldLocation);
            //convert the track object who represent the path in the map to a string DataType and put it in the intent
            i.putExtra(Keys.PATH , Converter.convertSessionPathToString(track));
            //change old location to the new location to get ready for the next calculations
            oldLocation = location;
        }
        //Convert LatLng Object to String DataType and put it into the Intent
        String latString = Converter.convertLatLngToString(ll);
        i.putExtra(Keys.LAT_LNG, latString);
        //send intent
        sendBroadcast(i);
    }


    //this Method will be executed when User Using the Miles as a Distance Unite in Settings
    private void processTheLocationInMile (Location location){
        //create LatLng  From Location That we received
        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
        //check the accuracy of the location
        if(location.getAccuracy() <= 20){
            /* When isConnected = true
            * That Mean The User Still Recording His Activity
            * So Calculate all the data and Put Them into The Intent and Send it to TrackingActivity (Client).
            *
            * When isConnected = false
            * That mean the user Stop Recording His Activity By pressing Pause Button in TrackingActivity (Client)
            * In This Case We Don't Calculate any Data (distance , elev ..)
            * But we still send To The Client The Current Position
            * And Path To TrackingActivity (Client)*/
            if(isConnected){
                //Get Speed From the Location Object Using getSpeed That return speed in meters
                //Convert Speed from meters to the Mils
                double speedMiles = (location.getSpeed() / 1609.344) * 3600;
                double elevation = location.getAltitude();

                //Calculate Best Speed
                if (speedMiles > bestSpeed) {
                    bestSpeed = speedMiles;
                }

                //Calculate The Highest Elevation
                if (elevation > highestElevationRecorded){
                    highestElevationRecorded = elevation;
                }

                //Distance Calculation
                if (oldLocation != null) {

                    //Calculate Distance btw two locations (the Value return to as with meters unite)
                    double disMeter = location.distanceTo(oldLocation);
                    //Convert Meters to Miles
                    double disMile = disMeter / 1609.344;
                    //We got the Current Distance Here
                    disSum = disSum + disMile;

                    //play audio feedback about the tracking
                    audioFeedBack.playAudio(baseTime , disSum);

                    //add data to the Intent
                    i.putExtra(Keys.SPEED, speedMiles);
                    i.putExtra(Keys.DISTANCE, disSum);
                    i.putExtra(Keys.BEST_SPEED , bestSpeed);
                    i.putExtra(Keys.ELEVATION , elevation);
                }
            }

            //Append Path Data And Convert Array into String and Put into The intent and Send it to the Client
            preparePathData(location , oldLocation);
            i.putExtra(Keys.PATH , Converter.convertSessionPathToString(track));



            //change old location to the new one to get ready for the new calculation when we get updates
            oldLocation = location;
        }

        //Convert LatLng Object to String and put it inside the Intent to show the user current place !
        String latString = Converter.convertLatLngToString(ll);
        i.putExtra(Keys.LAT_LNG, latString);

        //send the intent
        sendBroadcast(i);
    }


    //prepare path data from the received locations
    private void preparePathData (Location currentLocation , Location previousLocation){
        if(previousLocation != null){
            //when distance = 0
            // that mean user don't move from his previous location
            // and it not necessary to prepare any data fot the path
            if(currentLocation.distanceTo(previousLocation) > 0){

                //Create LatLng
                LatLng ll = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                //

                if (isConnected){
                    //when client recording his 'workout' use accent color as as a color for the path
                    track.appendPathData(ll , getResources().getColor(R.color.colorAccent));
                }else {
                    //when client stop recording his 'workout' the use gray as color for the path
                    track.appendPathData(ll , getResources().getColor(R.color.gray));
                }

            }
        }
    }


    //This Method get call when Client all ready bounded to the service and clicked Pause Button in Map Activity !
    public void pause (String timetext) {
        //changeDistanceUnite the Custom chronometer time text
        chronometerText = timetext;
        //get current time
        oldTime = SystemClock.elapsedRealtime();
        //add new polyline to show a path tracked distance.
        track.newPolylineInstance();
        isConnected = false;
    }


    /**Method called when Client clicked Resume Button in TrackingActivity
    * - Calculate The New Base Time
    * - change isConnected to True* */
    @SuppressLint("MissingPermission")
    public void resume() {
        //Select the new Base time for the Chronometer
        long currentTime = SystemClock.elapsedRealtime();
        long waistedTime = currentTime - oldTime;
        //get us the New Base Time
        baseTime = baseTime + waistedTime ;
        //add new polyline to show a path for non tracked distance.
        track.newPolylineInstance();
        isConnected = true;
    }


    //This Method Allow's us to Know if user Still Recording His Session or just his press Pause Button  !
    boolean isConnected (){
        return isConnected;
    }


    //return last value of baseTime
    long getBaseTime (){
        return baseTime;
    }


    //This Method it's Use Full in Calculation of the Time in different Cases !
    long getPauseSystemTime (){
        return oldTime;
    }


    //Return the last text of the Chronometer That was in Before !
    String getLastTextOfChronometer (){
        return chronometerText;
    }


    //return the state of the screen (locked or unlocked)
    boolean isScreenLocked (){
        return isScreenLocked;
    }


    //notify the service is the client locked or unlocked the screen
    void changeLockMood (Boolean state){
        isScreenLocked = state;
    }


    //This Method called when Client Click Finish Button in Map Activity !
    ActivityInformation saveAllData (double TimeInMs){
        //set the title
        activityInformation.setTitle(DateFormat.getDateTimeInstance().format(new Date()));
        //set distanceUnit this activity_history recorded with
        activityInformation.setUnit(distanceUnit);
        //Set the Best Speed
        activityInformation.setBestSpeed(bestSpeed);
        //Set Distance
        activityInformation.setDistance(disSum);
        //Save Path As String Data Type
        activityInformation.setPath(Converter.convertSessionPathToString(track));
        //Set Time In Hour to Help Us to get The Average Speed
        activityInformation.setTimeInMs(TimeInMs);
        //set Highest Elevation Recorded
        activityInformation.setHighestElevationRecorded(highestElevationRecorded);
        //Disconnect the User form Location Provider !
        locationManager.removeUpdates(locationHandler);
        //stop service
        stopSelf();
        //Return this Object to Map Activity
        return activityInformation;
    }


    //called when the user finish activity under 20 sec and it remove location updates and stop the service without any saved data
    void workoutCanceled(){
        //Disconnect the User form Location Provider !
        locationManager.removeUpdates(locationHandler);
        //stop service
        stopSelf();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        //close the audio
        audioFeedBack.closeTts();
        //check the android version
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.cancel(notification_id);
        }else {
        //remove the Notification Bar The Tracking
        stopForeground(true);
        }
    }


    //class used to listen for location updates
    private class locationHandler implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            /*Location Maybe null in this cases below
             *1 The Location is turned off in the Settings !
             *2 The Device Never Recorded it's Location Before !
             *3 Google Play Services Has restarted in the Device !*/
            if (location != null) {
                //check the Distance Unite in user activity_settings
                // and chose the right method to do the right calculation
                //When distanceUnit = 0 That mean the User Chose Kilometer as Distance Unite For The App !
                if (distanceUnit == 0) {
                    //km
                    processTheLocationInKm(location);
                }else {
                    //mile
                    processTheLocationInMile(location);
                }
            }
        }



        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }


        @Override
        public void onProviderEnabled(String provider) {

        }


        @Override
        public void onProviderDisabled(String provider) {

        }
    }



    //this class used to perform audio feedback for the tracking details
    private class AudioFeedBack implements TextToSpeech.OnInitListener{
        private SettingManager settingManager;
        private TextToSpeech textToSpeech;
        private String distanceUnite;//distance Unite holder
        private boolean isAudioOnInSetting;//boolean to know is sound on or off
        private double feedbackRateHelper;

        //Constructor
        AudioFeedBack (SettingManager settingManager){

            //Text to Speech
            this.textToSpeech = new TextToSpeech(getApplicationContext() , this);

            //Setting Manager
            this.settingManager = settingManager;
            //Default unite distance unite is kilometers
            if (settingManager.getDistanceUniteSettings() == SettingManager.DEFAULT_UNIT_VALUE){
                this.distanceUnite = "kilometers";
            }else {
                this.distanceUnite = "miles";
            }

            //Boolean to determine the audio is on or off in user activity_settings
            this.isAudioOnInSetting = settingManager.getSoundSettings();

            //feedback rate audio (play audio each x distance)
            this.feedbackRateHelper = settingManager.getAudioFeedbackRateValue();
        }



        @Override
        //Callback get Call after the Text To Speech Engine installation Completed
        public void onInit(int status) {
            //check for the status of  Engine installation!
            if(status == TextToSpeech.SUCCESS){
                //set Language for the TTS
                textToSpeech.setLanguage(Locale.ENGLISH);
                //Configure the speech rate
                textToSpeech.setSpeechRate(0.75f);
            }
        }



        //This method get called each time we get updates but we have the right statements and logic
        //That play the audio in right time depends on the user activity_settings
        void playAudio (double baseTime , double distance){
            //just play audio when isAudioOnInSetting is = to true
            if(isAudioOnInSetting){
                //Check if we should we play audio and this depends of the user current distance
                if(distance >= feedbackRateHelper){
                    //Calculate Total Time for the
                    double totalTimeInMs = SystemClock.elapsedRealtime() - baseTime;
                    double totalTimeInSec = totalTimeInMs / 1000;
                    //construct time format
                    int hh = (int)(totalTimeInSec / 3600);
                    int mm = (int)((totalTimeInSec % 3600) / 60);
                    int ss = (int)(totalTimeInSec % 60);

                    //string var Hold the text to be the speech for the user
                    String speech;

                    //when time under one hour just play minutes and seconds
                    if (hh == 0){
                        speech = "Time" + mm + "minutes" + ss + "seconds"
                                +"current distance" + feedbackRateHelper + distanceUnite;

                    }else {
                        //play time normally when time up one hour
                        speech = "Time" + hh + "hour" + mm + "minutes" + ss + "seconds"
                                +"current distance" + feedbackRateHelper + distanceUnite;
                    }

                    //when time under one hour just play the audio for minutes and seconds
                    textToSpeech.speak(speech
                            ,TextToSpeech.QUEUE_FLUSH
                            ,null);


                    //increase the value of the feedback helper to get ready for the next calculation
                    feedbackRateHelper += settingManager.getAudioFeedbackRateValue();
                }
            }
        }



        //close the text speech engine
        void closeTts (){
            textToSpeech.shutdown();
        }
    }
}
