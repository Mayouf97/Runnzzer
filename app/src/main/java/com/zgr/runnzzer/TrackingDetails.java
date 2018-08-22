package com.zgr.runnzzer;


/*
 * This activity can be opened by different activities (TrackingActivity && History)!
 * */

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.ContextThemeWrapper;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import sqlitedataBase.DataBaseFeedEntry;
import sqlitedataBase.DataBaseHelper;
import tracking.Track;
import tracking.TrackingActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class TrackingDetails extends AppCompatActivity implements OnMapReadyCallback {

    public static final String WHO = "who"; //define who open this activity
    public static final int MAP_OPENED_ME = 0 ;
    static final int SESSIONS_OPENED_ME = 1 ;
    static final int EDIT_ACTIVITY = 2;
    private DataBaseHelper dataBaseHelper;//DataBase Collection
    private int Id;//activity_history Id
    private HashMap<String , String> hashMap;
    private boolean wasExpend;//var help as to switch btw map expand and Tight
    private GoogleMap mMap;//represent Map
    private SettingManager settingManager;//activity_settings Manager
    private String currentDistanceUnite;
    private int who_open_me;
    private ConstraintLayout mapLayout;
    private ImageView zoomButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_details);
        //get Intent who open this Activity and Extract activity id  !
        Bundle b = getIntent().getExtras();
        //check who open the map !
        assert b != null;
        who_open_me = b.getInt(WHO);
        //if the int equal 0 means this activity opened by live Tracking activity else mean by the activity_history activity

        switch (who_open_me){
            //
            case MAP_OPENED_ME :
                //find menu icon and make it invisible
                findViewById(R.id.tracling_details_menu_icon).setVisibility(View.INVISIBLE);
                //Access to DataBase
                dataBaseHelper = new DataBaseHelper(this);
                //get the last id in sessions table
                Id = dataBaseHelper.readAvailableIds().get(dataBaseHelper.readAvailableIds().size() - 1);
                //Get HashMap of Strings from the DataBase (of One Session) by row id
                hashMap = dataBaseHelper.readSessionByRowId(Id);
                break;
            //
            case SESSIONS_OPENED_ME :
                //Access to DataBase
                dataBaseHelper = new DataBaseHelper(this);
                //get Last id in activity_history table have been added
                Id = b.getInt("id");
                //Get the HashMap from dataBase
                hashMap = dataBaseHelper.readSessionByRowId(Id);
                break;
            //
            case EDIT_ACTIVITY :
                //Access to DataBase
                dataBaseHelper = new DataBaseHelper(this);
                //get id
                Id = b.getInt(Keys.ID);
                //Get the HashMap from dataBase
                hashMap = dataBaseHelper.readSessionByRowId(Id);
                break;
        }
        //set up activity_settings manager
        settingManager = new SettingManager(getSharedPreferences(SettingManager.SETTINGS_FILE_NAME , MODE_PRIVATE));
        //
        setUpCurrentDistance();
    }


    @Override
    protected void onResume() {
        super.onResume();
		  //Obtain the SupportMapFragment
		  SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		  //map sync
		  mapFragment.getMapAsync(this);
        //Set Up Details Information
        setUpUi();
    }


	 @Override
	 public void onMapReady(GoogleMap googleMap) {
		  mMap = googleMap;
		  //Check If GoogleMap Object Don't equal to null
		  if(mMap != null){
				//Set Map Style Depends on User Settings
				switch (settingManager.getMapSettings()){

					 case SettingManager.DEFAULT_MAP :
						  //set Dark style
						  mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
						  break;

					 case SettingManager.SATELLITE_MAP :
						  //set Satellite style
						  mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
						  break;

					 case SettingManager.TERRAIN_MAP :
						  //set terrain style
						  mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
						  break;
				}
				//draw the activity_history Path Into the Map
				pathDrawer();
		  }
	 }



    private void setUpUi (){
        mapLayout = findViewById(R.id.map_layout);
        zoomButton = findViewById(R.id.zoom_button);

        //title
        setTitle();
        //time
        setTimeText();
        //distance
        setDistanceText();
        //elevation
        setElevationText();
        //average speed
        averageSpeedText();
    }


    private void setUpCurrentDistance (){
        if (settingManager.getDistanceUniteSettings() == SettingManager.DEFAULT_UNIT_VALUE){
            //Kilometers
            currentDistanceUnite = getResources().getString(R.string.km);
        }else {
            //Miles
            currentDistanceUnite = getResources().getString(R.string.mi);
        }
    }


    private void setTitle (){
        //
        TextView title = findViewById(R.id.run_details_title);
        //
        String titleString = hashMap.get(DataBaseFeedEntry._TITLE);
        //
        title.setText(titleString);
    }


    private void setTimeText (){
        //find time text view
        TextView time = findViewById(R.id.run_details_duration);
        //get time as string data type from the hash map
        String timeAsString = hashMap.get(DataBaseFeedEntry._TIMEINMS);
        //convert time to double
        double timeInMs = Double.parseDouble(timeAsString);
        //
        String formattedTime = Converter.timeFormatter(timeInMs);
        //set text
        time.setText(formattedTime);
    }


    private void setDistanceText (){
        //Find Distance Text View
        TextView distanceTitle = findViewById(R.id.run_details_distance);
        //
        String distanceAsString = hashMap.get(DataBaseFeedEntry._DISTANCE);
        //
        String usedDistanceUniteAsString = hashMap.get(DataBaseFeedEntry._UNITEUSED);
        //
        int parsedDistanceUnite = Integer.parseInt(usedDistanceUniteAsString);
        //
        int actualDistanceUniteInSettings = settingManager.getDistanceUniteSettings();
        //
        double distanceValue = Double.parseDouble(distanceAsString);
        //
        if(parsedDistanceUnite != actualDistanceUniteInSettings){
            if (actualDistanceUniteInSettings == SettingManager.DEFAULT_MAP){
                //km so convert to mi
                distanceValue = Converter.convertKiloMeterToMile(distanceValue);
            }else {
                distanceValue = Converter.convertMileToKiloMeter(distanceValue);
            }
        }
        //
        String formattedDecimalDistanceValue = new DecimalFormat("#.##").format(distanceValue);
        //
        String distanceTextFormat = String.format(Locale.ENGLISH , "%s %s" , formattedDecimalDistanceValue , currentDistanceUnite);
        //
        distanceTitle.setText(distanceTextFormat);
    }


    private void setElevationText (){
        //installing text view of the elevation
        TextView elevation = findViewById(R.id.elevation_gain);
        //get elevation from hash map as string data type
        String elevationAsString = hashMap.get(DataBaseFeedEntry._HIGHESTELEVATION);
        //convert stringElevation to double value
        double elevationValue =  Double.parseDouble(elevationAsString);
        //format the decimal elevation value
        String formattedElevationValue = new DecimalFormat("#.##").format(elevationValue);
        //elevation Text format
        String elevationTextFormat = String.format(Locale.ENGLISH , "%s %s" , formattedElevationValue , "m");
        //set the text
        elevation.setText(elevationTextFormat);
    }


    private void averageSpeedText (){
        //
        TextView averageSpeed = findViewById(R.id.run_details_averageSpeed);
        //get average speed value from the hash map as string
        String avgAsString = hashMap.get(DataBaseFeedEntry._AVERAGESPEED);
        //
        double distanceValue = Double.parseDouble(avgAsString);
        //
        int actualDistanceInSettings = settingManager.getDistanceUniteSettings();
        //
        int usedUnite = Integer.parseInt(hashMap.get(DataBaseFeedEntry._UNITEUSED));
        //
        if(usedUnite != actualDistanceInSettings){
            if (actualDistanceInSettings == SettingManager.DEFAULT_MAP){
                //km so convert to mi
                distanceValue = Converter.convertKiloMeterToMile(distanceValue);
            }else {
                distanceValue = Converter.convertMileToKiloMeter(distanceValue);
            }
        }
        //
        String formattedDistanceValue = new DecimalFormat("#.##").format(distanceValue);
        //
        String averageSpeedText = String.format(Locale.ENGLISH , "%s %s/h" , formattedDistanceValue , currentDistanceUnite);
        //
        averageSpeed.setText(averageSpeedText);
    }


	 //Draw path in the map
	 private void pathDrawer (){
		  //Get Path From DataBase As String And Convert it to Session Path Object
		  Track track = Converter.convertStringToSessionPath(hashMap.get(Keys.PATH));
		  if (track != null){
				ArrayList<PolylineOptions> pathArrayList = track.getPathArrayList();
				/*If the pathArrayList Don't Empty Draw the Polylines Contained in it */
				if (!pathArrayList.isEmpty()) {

					 //Draw The Path In The Map
					 for (PolylineOptions po : pathArrayList) {
						  mMap.addPolyline(po);
					 }

					 /*Determine First Location and Add Marker On it
					  *Get First PolylineOption in The pathArrayList
					  * And get the First Point from it Using getPoints()
					  */
					 LatLng firstLatLng = pathArrayList.get(0).getPoints().get(0);
					 //add Marker
					 mMap.addMarker(new MarkerOptions()
								.position(firstLatLng)
								.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_start_marker)));

					 /*Determine last Location and Add Marker On it
					  *Get last PolylineOption in The pathArrayList
					  * And get the last Point from it Using getPoints
					  */
					 List<LatLng> points = pathArrayList.get(pathArrayList.size() - 1).getPoints();
					 LatLng lastLatLng = points.get(points.size() - 1);
					 //add marker
					 mMap.addMarker(new MarkerOptions()
								.position(lastLatLng)
								.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_end_marker)));
					 //Move Camera to The last Position
					 mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, TrackingActivity.MAP_CAMERA_ZOOM_LEVEL));
				}
		  }


	 }




    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //
        switch (who_open_me){

            //
            case MAP_OPENED_ME :
                //Access to DataBase
                //start home activity
                startActivity(new Intent(this , MainActivity.class));
                break;

            //
            case SESSIONS_OPENED_ME :
                //kill activity if this activity opened by activity_history activity
                finish();
                break;
            //
            case EDIT_ACTIVITY :
                //kill activity if this activity opened by activity_history activity
                finish();
                break;
        }
        //Close the activity by left_right transition
		  Transitions.closeTransition(this);
    }


	 //event handling for back arrow
	 public void backArrowClick (View view){
		  onBackPressed();
	 }




	 public void onClick (View view){
    	 switch (view.getId()){
			  case R.id.zoom_button :
			  	 handleZoomButtonClicking();
			  	 break;
		 }
	 }



	 private void handleZoomButtonClicking (){
    	 //Find Layout that contain Map Fragment
		  ViewGroup.LayoutParams layoutParams = mapLayout.getLayoutParams();
		  if (!wasExpend){
              zoomButton.setImageResource(R.mipmap.ic_exit_fullscreen);
		  	   //Find Root Layout for the activity
				LinearLayout linearLayout = findViewById(R.id.run_details_layout);
				layoutParams.height = linearLayout.getHeight();
		  	    mapLayout.setLayoutParams(layoutParams);
		  	  wasExpend = true;
		  }else {
              zoomButton.setImageResource(R.mipmap.ic_full_screen);
				layoutParams.height = (int) getResources().getDimension(R.dimen.map_height);
				mapLayout.setLayoutParams(layoutParams);
		  	    wasExpend = false;
		  }
	 }




    //event click handler for the pop-up menu
    public void menuClicked(View view) {
        //Custom style to the Pop Up Menu
        ContextThemeWrapper wrapper = new ContextThemeWrapper(this , R.style.PopUpStyle);
        PopupMenu popup = new PopupMenu(wrapper , view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.tracking_details_menu , popup.getMenu());
        popup.show();
        //
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()){
                //
                case R.id.tracking_details_edit :
                    editActivityInformation();
                    break;

                //
                case R.id.tracking_details_delete :
                    delete();
                    break;
                //
            }

            Transitions.openTransition(this);
            return true;
        });
    }


    //edit activity_history
	 private void editActivityInformation(){
		  //Intent to Open EditSessionInformation Activity
		  Intent i = new Intent(this , EditActivityInformation.class);
		  //Put Information into the intent
		  i.putExtra(Keys.TITLE , hashMap.get(DataBaseFeedEntry._TITLE));
		  i.putExtra(Keys.DURATION , hashMap.get(DataBaseFeedEntry._TIMEINMS));
		  i.putExtra(Keys.DISTANCE , hashMap.get(DataBaseFeedEntry._DISTANCE));
		  i.putExtra(Keys.AVERAGE_SPEED , hashMap.get(DataBaseFeedEntry._AVERAGESPEED));
		  i.putExtra(Keys.UNITE_USED , hashMap.get(DataBaseFeedEntry._UNITEUSED));
		  i.putExtra(Keys.MAX_ELEVATION , hashMap.get(DataBaseFeedEntry._HIGHESTELEVATION));
		  i.putExtra(Keys.ID , Id);
		  //start the new activity
		  startActivity(i);
		  finish();
	 }


	 //delete this activity data row from the database
	 public void delete (){
		  //delete the row in activity_history table via id
		  dataBaseHelper.deleteActivityById(Id);
		  //Kill this activity
		  finish();
		  //
		  onBackPressed();
	 }

}