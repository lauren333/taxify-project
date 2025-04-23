package project.taxify;
import java.time.LocalDate;
//import project.taxify.MicroMobility.IMicroMobilityVehicle;

public interface IUser {

    public int getId();
    public String getFirstName();
    public String getLastName();
    public char getGender();
    public LocalDate getBirthDate();
    public boolean getService();
    public void setService(boolean service);
    public void setCompany(ITaxiCompany company);
    public void requestService();
    public void rateService(IService service);
    public String toString();

    //new fromt sprint 4
    public boolean getPinkServiceEligibility(); //new from sprint 4: applicable if women or child
    public void requestPinkService();
    public void requestSilentService();
    public boolean isAllowedToShareFlag();
    public boolean isFemale();
    public boolean isChild();

    //new from sprint 5
    ///*  
    public void requestMicroMobilityService(); 
    //public void walkToMicroMobilityVehicle(IMicroMobilityVehicle vehicle);
    //public void setLocation(ILocation location); 
    //public ILocation getLocation();
    //public boolean hasArrivedAtVehicle(IMicroMobilityVehicle microVehicle); 
    //*/
}
