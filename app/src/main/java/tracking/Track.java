package tracking;


import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;


public class Track {


    private int currentIndex = 0;//represent current index
    private ArrayList<PolylineOptions> pathArrayList = new ArrayList<>();//Holds all the polylineOption we create
    private PolylineOptions p = new PolylineOptions();//temporary member they hold collection of data for period of time



    //Get LatLng and color to create polylineOption Object
    void appendPathData (LatLng l1  , int color){

        //add point to the current
        p.add(l1).color(color).width(8f);
        //if the list is empty then add the first polyline to it
        if (pathArrayList.size() == 0){

            //add polyline
            pathArrayList.add(p);

        }else {
            //check if the list need to expand or not
            if (currentIndex + 1 == pathArrayList.size()){

                //change the value in the currentIndex
                pathArrayList.set(currentIndex , p);

            }else {

                //add new Polyline Option to the list
                pathArrayList.add(p);
            }
        }

    }



    void newPolylineInstance (){
        //when polyline don't  Contain any LatLng
        //it not necessary to create new Instance
        //still using the current one
        if (p.getPoints().size() > 0){
            p = new PolylineOptions();
            currentIndex ++;
        }
    }


    //return array list
    public ArrayList<PolylineOptions> getPathArrayList (){
        return pathArrayList;
    }



}
