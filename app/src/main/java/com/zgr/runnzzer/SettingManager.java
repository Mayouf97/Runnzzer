package com.zgr.runnzzer;

import android.content.SharedPreferences;

public class SettingManager {

    //File Name
    public static final String SETTINGS_FILE_NAME = "activity_settings";
    //Unites
    private static final String UNITE_KEY = "unit";
    public static final int DEFAULT_UNIT_VALUE = 0;
    private static final int MILE_UNIT_VALUE = 1;
    //Map And Styles
    private static final String MAP_KEY = "map";
    public static final int DEFAULT_MAP = 0;
    public static final int SATELLITE_MAP = 1;
    public static final int TERRAIN_MAP = 2;
    //Feed Back Rate Key/Default Value
    private static final String KEY_AUDIO_FEEDBACK_RATE = "feedBack rate";
    private static final float DEFAULT_AUDIO_FEEDBACK_RATE_VALUE = 0.5f;
    //Audio
    private static final String AUDIO_KEY = "audio";
    private static final boolean AUDIO_DEFAULT_VALUE = true;
    //Shared Preferences
    private SharedPreferences sharedPreferences;



    public SettingManager (SharedPreferences sharedPreferences){
        this.sharedPreferences = sharedPreferences;
    }


    //return the current value in settings (0 km || 1 mi)
    public int getDistanceUniteSettings(){
        return sharedPreferences.getInt(UNITE_KEY , DEFAULT_UNIT_VALUE);
    }



    //change the unite value in the settings
    void changeDistanceUniteSetting(){
        //get the current value and change value to the opposite value
        int currentUnite = sharedPreferences.getInt(UNITE_KEY , DEFAULT_UNIT_VALUE);
        //current -> km change to ->mi
        if (currentUnite == DEFAULT_UNIT_VALUE){
            sharedPreferences.edit().putInt(UNITE_KEY , MILE_UNIT_VALUE).apply();
        }else {
            //current -> mi change to -> km
            sharedPreferences.edit().putInt(UNITE_KEY , DEFAULT_UNIT_VALUE).apply();
        }
    }




    //Map Style Settings
    public int getMapSettings (){
        return sharedPreferences.getInt(MAP_KEY , DEFAULT_MAP);
    }



    //
    public void changeMapSettings (int mapId){
        sharedPreferences.edit().putInt(MAP_KEY , mapId).apply();
    }



    //Audio Feed Back Rate
    void changeAudioFeedBackRate (float value){
        sharedPreferences.edit().putFloat(KEY_AUDIO_FEEDBACK_RATE , value).apply();
    }



    //return the current value of the feedback rate in the settings
    public float getAudioFeedbackRateValue(){
        return sharedPreferences.getFloat(KEY_AUDIO_FEEDBACK_RATE , DEFAULT_AUDIO_FEEDBACK_RATE_VALUE);
    }




    //return the state of the sound is off or on
    public boolean getSoundSettings (){
        return sharedPreferences.getBoolean(AUDIO_KEY , AUDIO_DEFAULT_VALUE);
    }




    //turn sound on or off
    void changeSoundSettings (){
        if (sharedPreferences.getBoolean(AUDIO_KEY , AUDIO_DEFAULT_VALUE)){
            sharedPreferences.edit().putBoolean(AUDIO_KEY , false).apply();
        }else{
            sharedPreferences.edit().putBoolean(AUDIO_KEY , true).apply();
        }
    }





    //USER PROFILE
    static final String PROFILE_FILE_NAME = "profile";
    //Age
    private static String AGE_KEY_VALUE = "age";
    private static int AGE_DEFAULT_VALUE = 0;
    //gender
    static final String GENDER_KEY = "gender";
    final String GENDER_DEFAULT_VALUE = "?";
    static final String MALE = "male";
    static final String FEMALE = "female";
    //weight
    static final String WEIGHT_KEY = "weight";
    static final int WEIGHT_VALUE_KEY = 0;
    //height
    static final String HEIGHT_KEY = "height";
    static final int HEIGHT_VALUE_KEY = 0;


    //Age Methods
     int getAge (){
        return sharedPreferences.getInt(AGE_KEY_VALUE , AGE_DEFAULT_VALUE);
    }

     void changeUserAge (int age){
        sharedPreferences.edit().putInt(AGE_KEY_VALUE , age).apply();
    }

    //Gender Methods
     String getGender (){
       return sharedPreferences.getString(GENDER_KEY , GENDER_DEFAULT_VALUE);
    }
     void changeGender (String gender){
        sharedPreferences.edit().putString(GENDER_KEY , gender).apply();
    }

    //weight
     int getWeight (){
        return sharedPreferences.getInt(WEIGHT_KEY , WEIGHT_VALUE_KEY);
    }


     void changeWeight (int weight){
        sharedPreferences.edit().putInt(WEIGHT_KEY , weight).apply();
    }

    //Height
    public int getHeight (){
        return sharedPreferences.getInt(HEIGHT_KEY , HEIGHT_VALUE_KEY);
    }

     void changeHeight (int height){
        sharedPreferences.edit().putInt(HEIGHT_KEY , height).apply();
    }











    //Display Settings
    //Keys
    private static final String FIRST_DISPLAY_KEY = "first_display_key";
    private static final String SECOND_DISPLAY_KEY = "second_display_key";
    private static final String THIRD_DISPLAY_KEY = "third_display_key";
    //Default Values
    private static final String FIRST_DISPLAY_DEFAULT_VALUE = Keys.DURATION;
    private static final String SECOND_DISPLAY_DEFAULT_VALUE = Keys.DISTANCE;
    private static final String THIRD_DISPLAY_DEFAULT_VALUE = Keys.SPEED;


    //First Display
    public String getFirstDisplay(){
        return sharedPreferences.getString(FIRST_DISPLAY_KEY, FIRST_DISPLAY_DEFAULT_VALUE);
    }
    public void changeFirstDisplay(String value){
        sharedPreferences.edit().putString(FIRST_DISPLAY_KEY , value).apply();
    }


    //Second Display
    public String getSecondDisplay (){
        return sharedPreferences.getString(SECOND_DISPLAY_KEY , SECOND_DISPLAY_DEFAULT_VALUE);
    }
    public void changeSecondDisplay(String value){
        sharedPreferences.edit().putString(SECOND_DISPLAY_KEY , value).apply();
    }


    //Third Display
    public String getThirdDisplay (){
        return sharedPreferences.getString(THIRD_DISPLAY_KEY , THIRD_DISPLAY_DEFAULT_VALUE);
    }
    public void changeThirdDisplay(String value){
        sharedPreferences.edit().putString(THIRD_DISPLAY_KEY , value).apply();
    }

}
