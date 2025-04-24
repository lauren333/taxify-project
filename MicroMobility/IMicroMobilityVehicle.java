package project.taxify.MicroMobility;
import project.taxify.ILocation;
import project.taxify.IService;
import project.taxify.IStatistics;
import project.taxify.VehicleStatus;
import project.taxify.ITaxiCompany;
//import project.taxify.IUser;

public interface IMicroMobilityVehicle{
    public int getId();
    public void setLocation(ILocation location);
    public ILocation getLocation();
    public ILocation getDestination();
    public IService getService();
    public IStatistics getStatistics(); 
    public void setCompany(ITaxiCompany company);
    public VehicleStatus getStatus();
    public double getBatteryLevel();
    public void chargeBattery();
    public void reserve(IService service);
    public void startRide();
    public void endRide();
    public void notifyArrivalAtPickupLocation();
    public void notifyArrivalAtDropoffLocation();
    public void move(); 
    public double calculateCost();
    public boolean isFree();
    public String toString();
}

