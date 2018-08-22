package tracking;




public class ActivityInformation {

    private String Title;
    private double BestSpeed;
    private double Distance;
    private int Unit;
    private String Path;
    private double TimeInMs;
    private double highestElevationRecorded;



    public void setTitle(String title) {
        Title = title;
    }



    public void setBestSpeed(double bestSpeed) {
        BestSpeed = bestSpeed;
    }



    public void setDistance(double distance) {
        Distance = distance;
    }



    public void setUnit(int unit) {
        Unit = unit;
    }



    public void setPath(String path) {
        Path = path;
    }



    public void setTimeInMs(double timeInHours){
        TimeInMs = timeInHours;
    }



    public void setHighestElevationRecorded (double highestElevationRecorded){
        this.highestElevationRecorded = highestElevationRecorded;
    }



    public String getTitle() {
        return Title;
    }



    public double getBestSpeed() {
        return BestSpeed;
    }



    public double getDistance() {
        return Distance;
    }



    public int getUnit() {
        return Unit;
    }



    public String getPath() {
        return Path;
    }



    public double getTimeInMs(){
        return TimeInMs;
    }



    public double getHighestElevation (){
        return highestElevationRecorded;
    }



    public double getAverageSpeed() {
        if(TimeInMs == 0){
            return 0;
        }
        return  Distance / (TimeInMs / (1000*60*60));
    }

}
