package project.taxify.MicroMobility;
import project.taxify.ILocation;
import project.taxify.IService;
import project.taxify.IStatistics;
import project.taxify.VehicleStatus;
import project.taxify.ITaxiCompany;
import project.taxify.IUser;

public interface IMicroMobilityVehicle{
    int getId();
    ILocation getLocation();
    ILocation getDestination();
    IService getService();
    void setCompany(ITaxiCompany company);
    VehicleStatus getStatus();
    double getBatteryLevel();
    void chargeBattery();
    void reserve(IService service);
    void startRide();
    void endRide();
    void notifyArrivalAtPickupLocation();
    void notifyArrivalAtDropoffLocation();
    double calculateCost();
    //void setLocation(ILocation location);
    String toString();
    void move(); 
    boolean isFree();
    //void setUser(IUser user); 
    IUser getUser(); 
    IStatistics getStatistics(); 
}

