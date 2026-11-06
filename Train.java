public class Train {
    private String trainId;
    private String trainName;
    private String route;
    private String departureTime;
    private int totalSeats;
    private int bookedSeats;

    public Train(String trainId, String trainName, String route, String departureTime, int totalSeats, int bookedSeats) {
        this.trainId = trainId;
        this.trainName = trainName;
        this.route = route;
        this.departureTime = departureTime;
        this.totalSeats = totalSeats;
        this.bookedSeats = bookedSeats;
    }

    public String getTrainId() { return trainId; }
    public String getTrainName() { return trainName; }
    public String getRoute() { return route; }
    public String getDepartureTime() { return departureTime; }
    public int getTotalSeats() { return totalSeats; }
    public int getBookedSeats() { return bookedSeats; }

    public int getAvailableSeats() {
        return totalSeats - bookedSeats;
    }

    public boolean bookSeats(int count) {
        if (getAvailableSeats() >= count) {
            this.bookedSeats += count;
            return true;
        }
        return false;

    }

    public void releaseSeats(int count) {
        this.bookedSeats = Math.max(0, this.bookedSeats - count);
    }

    
    public String toFileString() {
        return trainId + "," + trainName + "," + route + "," + departureTime + "," + totalSeats + "," + bookedSeats; 
    } 
}
