package project.taxify;
import project.taxify.MicroMobility.IMicroMobilityVehicle;

public interface ITaxiCompany {

    public String getName();    
    public int getTotalServices();
    public void arrivedAtPickupLocation(IVehicle vehicle);  
    public void arrivedAtPickupLocation2(IMicroMobilityVehicle microMobilityVehicle);  
    public void arrivedAtDropoffLocation(IVehicle vehicle); //new from sprint 2
    public void arrivedAtDropoffLocation2(IMicroMobilityVehicle microMobilityVehicle);  
    public boolean provideService(int user, ILocation origin, ILocation destination); //updated in sprint 4
    public boolean provideSharedService(int userId, ILocation origin, ILocation destination); //new from sprint 4
    public boolean providePinkService(int user, ILocation origin, ILocation destination); //new from sprint 4
    public boolean provideSilentService(int user, ILocation origin, ILocation destination); //new from sprint 4

    public boolean provideMicroMobilityService(int userId, ILocation origin, ILocation destination); //new sprint 5
}
