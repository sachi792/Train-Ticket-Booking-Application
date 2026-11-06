import java.util.*;

public class Booking {

    private final String id, trainId, customerId, name, phone;

    private final List<Integer> seats;

    public Booking(String id,String trainId,String customerId,String name,String phone,List<Integer> seats){
        this.id=id;this.trainId=trainId;this.customerId=customerId;this.name=name;this.phone=phone;this.seats=new ArrayList<>(seats);
    }

    public String getId(){return id;} public String getTrainId(){return trainId;}

    public String getCustomerId(){return customerId;} public String getName(){return name;}

    public String getPhone(){return phone;} public List<Integer> getSeats(){return seats;}

    public String toFileString(){StringJoiner j=new StringJoiner(";");for(int n:seats)j.add(""+n);
        return String.join(",",id,trainId,customerId,name,phone,""+seats.size(),j.toString());}
        
}
