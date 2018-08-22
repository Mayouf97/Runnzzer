package com.zgr.runnzzer;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.shawnlin.numberpicker.NumberPicker;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;


public class UserProfile extends AppCompatActivity {

    private static final int from_camera_app = 0;
    private static final int from_gallery_app = 1;
    private ImageView profileImage;
    private Button age,gender,weight, height;
    private SettingManager settingManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        //Get Shared Preferences
        SharedPreferences sharedPreferences = getSharedPreferences(SettingManager.PROFILE_FILE_NAME, MODE_PRIVATE);
        //Get Setting Manager
        settingManager = new SettingManager(sharedPreferences);

        initializeViews();
        setUpUi();
    }


    private void initializeViews (){
        //find the views
        age = findViewById(R.id.user_age);
        gender = findViewById(R.id.user_gender);
        weight = findViewById(R.id.user_weight);
        height = findViewById(R.id.user_height);
        profileImage = findViewById(R.id.user_image);
    }


    private void setUpUi (){
        //set User Age
        age.setText(String.format(Locale.ENGLISH , "%s Y" , settingManager.getAge()));

        //User Gender
        gender.setText(settingManager.getGender());

        //User Weight
        weight.setText(String.format(Locale.ENGLISH , "%s Kg" , settingManager.getWeight()));

        //User Height
        height.setText(String.format(Locale.ENGLISH , "%s Cm" , settingManager.getHeight()));
    }


    //Clicks handler
    public void onClick (View view){
        //
        switch (view.getId()){
            case R.id.user_image :
                //
                selectImageSource();
                break;
            case R.id.user_age :
                //
                selectAge();
                break;
            case R.id.user_gender :
                //
                selectGender();
                break;
            case R.id.user_weight :
                //
                selectWeight();
                break;
            case R.id.user_height :
                //
                selectHeight();
                break;
        }
    }


    private void selectImageSource() {
        final AlertDialog alertBox = new AlertDialog.Builder(this).create();
        //inflate the view to in the dialog
        View view = createSmallAlertBox(R.layout.select_image_source);
        //Get Button from the dialog
        Button gallery = view.findViewById(R.id.from_gallery);
        Button camera = view.findViewById(R.id.from_camera);

        //set up listener for the gallery button
        gallery.setOnClickListener(e->{
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            if (intent.resolveActivity(getPackageManager()) != null){
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), from_gallery_app);
            }
            //close the dialog
            alertBox.dismiss();
        });

        //set up listener for the camera button
        camera.setOnClickListener(e->{
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, from_camera_app);
            }
            //close the dialog
            alertBox.dismiss();
        });

        alertBox.setView(view);
        alertBox.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){

            case  from_gallery_app:
                if (data != null){
                    Uri uri = data.getData();
                    try {
                        //Get the BitMap from the Intent
                        Bitmap mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        profileImage.setImageBitmap(mBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case from_camera_app:
                if (data != null){
                    //Get Bundle from the returned intent
                    Bundle extras = data.getExtras();

                    //Get the BitMap from the Intent
                    if(extras != null){
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        profileImage.setImageBitmap(imageBitmap);
                    }
                }
                break;
        }
    }




    //show date picker to the user to select his birthday
    private void selectAge (){
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) ->
        {
            //calculate age
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            int age =  ( currentYear - year);
            String ageValueString = String.valueOf(age);
            //set Text For the Button
            this.age.setText(String.format(Locale.ENGLISH , "%s Y" , ageValueString));
            //changeDistanceUnite age value in activity_settings
            settingManager.changeUserAge(age);
        }, 2018 , 1, 1);
        //
        datePickerDialog.show();

    }


    //show select Gender dialog
    private void selectGender() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        View view = createSmallAlertBox(R.layout.select_gender);
        //set up the listener for the buttons
        view.findViewById(R.id.select_gender_male).setOnClickListener(e->{
            //changeDistanceUnite the value in DataBase
            settingManager.changeGender(SettingManager.MALE);
            //changeDistanceUnite the Text of the Button
            gender.setText(SettingManager.MALE);
            //close the dialog
            alertDialog.dismiss();
        });
        view.findViewById(R.id.select_gender_female).setOnClickListener(e->{
            //changeDistanceUnite the value in DataBase
            settingManager.changeGender(SettingManager.FEMALE);
            //changeDistanceUnite the Text of the Button
            gender.setText(SettingManager.FEMALE);
            //close the dialog
            alertDialog.dismiss();
        });
        alertDialog.setView(view);
        alertDialog.show();
    }


    //select Weight PopUp Box
    private void selectWeight(){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        View view = createNormalAlertBox(R.layout.select_weight);
        NumberPicker numberPicker =  view.findViewById(R.id.select_weight_number_picker);
        numberPicker.setMinValue(20);
        numberPicker.setMaxValue(350);
        //
        if (settingManager.getWeight() != SettingManager.WEIGHT_VALUE_KEY){
            numberPicker.setValue(settingManager.getWeight());
        }else {
            numberPicker.setValue(70);
        }
        //action event respond !
        view.findViewById(R.id.set_weight_button).setOnClickListener(e->{
            int selectedValue = numberPicker.getValue();
            settingManager.changeWeight(selectedValue);
            weight.setText(String.format(Locale.ENGLISH , "%d Kg" , selectedValue));
            alertDialog.dismiss();
        });
        //
        alertDialog.setView(view);
        alertDialog.show();
    }


    //select Height PopUp Box
    private void selectHeight (){
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        View view = createNormalAlertBox(R.layout.select_height);
        NumberPicker numberPicker = view.findViewById(R.id.select_height_number_picker);
        //set the max and min value
        numberPicker.setMinValue(100);
        numberPicker.setMaxValue(230);
        //
        if (settingManager.getHeight() != SettingManager.HEIGHT_VALUE_KEY){
            numberPicker.setValue(settingManager.getHeight());
        }else {
            numberPicker.setValue(168);
        }
        //
        view.findViewById(R.id.set_height_button).setOnClickListener(e->{
            //set Text of the Button

            height.setText( String.format(Locale.ENGLISH , "%d Cm" , numberPicker.getValue()));
            //changeDistanceUnite the value in Shares Preferences
            settingManager.changeHeight(numberPicker.getValue());
            //close the AlertBox
            alertDialog.dismiss();
        });
        //
        alertDialog.setView(view);
        alertDialog.show();
    }


    //AlertBox Creator for normal dialogs
    private View createNormalAlertBox(int layoutResources){
        //Get screen display
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int h = metrics.heightPixels;
        int w = metrics.widthPixels;
        //get the layout
        View v = getLayoutInflater().inflate( layoutResources, null);
        //set sizes
        v.setMinimumWidth(w);
        v.setMinimumHeight((h / 2));
        return v;
    }


    //AlertBox Creator for small dialogs
    private View createSmallAlertBox(int layoutResources){
        //Get screen display
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int h = metrics.heightPixels;
        int w = metrics.widthPixels;
        //get the layout
        View v = getLayoutInflater().inflate( layoutResources, null);
        //set sizes
        v.setMinimumWidth(w);
        v.setMinimumHeight((h / 4));
        return v;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Transitions.closeTransition(this);
    }


    public void backArrow(View view) {
        onBackPressed();
    }
}
