package project.taxify;

public interface ITaxiCompany {

    public String getName();    
    public int getTotalServices();
    public boolean provideService(int user);
    void arrivedAtPickupLocation(IVehicle vehicle); //new from sprint 2
    void arrivedAtDropoffLocation(IVehicle vehicle); //new from sprint 2
}
