package project.taxify;
import java.time.LocalDate;
import java.time.Period;
//import project.taxify.MicroMobility.IMicroMobilityVehicle;

public class User implements IUser {
    private int id;
    private String firstName;
    private String lastName;
    private char gender;
    private LocalDate birthDate;
    private ITaxiCompany company;
    private boolean service;

    private ILocation location; // User's current location new fromt sprint 5 

    public User(int id, String firstName, String lastName, char gender, LocalDate birthDate) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthDate = birthDate;
        this.service = false;
        this.location = ApplicationLibrary.randomLocation(); // assign initial position new from sprint 5
    }
    
    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getFirstName() {
        return this.firstName;
    }
    
    @Override
    public String getLastName() {
        return this.lastName;
    }
    
    @Override
    public char getGender() {
       return this.gender;
    }

    @Override
    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    @Override
    public boolean getService() {
        return this.service;
    }
    
    @Override
    public void setService(boolean service) {
        this.service = service;
    }
    
    @Override
    public void setCompany(ITaxiCompany company) {
        this.company = company;
    }

    /* 
    @Override
    public ILocation getLocation() {
        return this.location;
    } */

    public void setLocation(ILocation location) {
        this.location = location;
    }
    
    @Override
    public void requestService() {
        ILocation origin = this.location;
        //ILocation origin = ApplicationLibrary.randomLocation();
        ILocation destination = ApplicationLibrary.randomLocation(origin);
        this.company.provideService(this.id, origin, destination);        
    }

    @Override
    public boolean isAllowedToShareFlag() { //set this to a random probability to accept sharing 
        return ApplicationLibrary.rand() % 2 == 0 ? false: true;    
    }

    @Override
    public boolean isFemale() { //new from sprint 4 to help calculate if appplicable for Pink Service 
        return (Character.toUpperCase(this.getGender()) == 'F') ? true : false;  
    }

    @Override
    public boolean isChild() { //new from sprint 4 to help calculate if appplicable for Pink Service 
        LocalDate todaysDate = LocalDate.now();
        Period timeElapsed = Period.between(this.getBirthDate(), todaysDate);
        int age = timeElapsed.getYears();
        return (age < 18) ? true : false; 
    }

    @Override
    public boolean getPinkServiceEligibility() { //new from sprint 4: applicable if women or child 
        return  (this.isChild() || this.isFemale()) ?  true: false;  
    }

    @Override
    public void requestPinkService() {
        ILocation origin = this.location;
        //ILocation origin = ApplicationLibrary.randomLocation();
        ILocation destination = ApplicationLibrary.randomLocation(origin);
        this.company.providePinkService(this.id, origin, destination);   
    }

    @Override
    public void requestSilentService() {
        ILocation origin = this.location;
        //ILocation origin = ApplicationLibrary.randomLocation();
        ILocation destination = ApplicationLibrary.randomLocation(origin);
        this.company.provideSilentService(this.id, origin, destination);    
    }
    

    @Override
    public void rateService(IService service) {
        // users rate around 50% of the services (1 to 5 stars)
        if (ApplicationLibrary.rand() % 2 == 0) {
            service.setStars(ApplicationLibrary.rand(5) + 1);
        }
    }
    
    @Override
    public String toString() {
        return this.getId() + " " + String.format("%-20s",this.getFirstName() + "  " + this.getLastName() + "  " + this.getGender() + "  " + this.getBirthDate());
        //ORIGINAL CODE: return this.getId() + " " + String.format("%-20s",this.getFirstName() + " " + this.getLastName());
    }


    //NEW FROM SPRINT 5 

      /*  
    @Override
    public void requestMicroMobilityService() {
        ILocation origin = this.location;
        //ILocation origin = ApplicationLibrary.randomLocation();
        ILocation destination = ApplicationLibrary.randomLocation(origin);
        this.company.provideMicroMobilityService(this.id, origin, destination);    
    }

    public boolean hasArrivedAtVehicle(IMicroMobilityVehicle microMobilityVehicle){ 
        return this.getLocation().equals(microMobilityVehicle.getLocation());
    }

    @Override
    public void walkToMicroMobilityVehicle(IMicroMobilityVehicle microMobilityVehicle) {
        ILocation currentLocation = this.getLocation(); // user's origin
        ILocation vehicleLocation = microMobilityVehicle.getLocation(); // vehicle's position when reserved

        //ILocation currentLocation = microMobilityVehicle.getService().getPickupLocation(); // user's origin
        //ILocation vehicleLocation = microMobilityVehicle.getLocation(); // vehicle's position when reserved

        // simulate walking by creating a route and updating step-by-step
        IRoute walkingRoute = new Route(currentLocation, vehicleLocation);

        // Get the type of micromobility vehicle
        String vehicleType = microMobilityVehicle.getClass().getSimpleName();
        System.out.println("User " + this.firstName + " " + this.lastName + " is walking to their " + vehicleType + " located at " + vehicleLocation);

        while (walkingRoute.hasLocations()) {
            currentLocation = walkingRoute.getNextLocation();
            this.setLocation(currentLocation); // Update user's actual location
            System.out.println("User " + this.firstName + " " + this.lastName + "walking to " + vehicleType + " Vehicle, now at: " + currentLocation);
        }
        // Ensure final position matches vehicle's location
        this.setLocation(vehicleLocation);

        System.out.println("User " + this.firstName + " " + this.lastName + " has arrived at the " + vehicleType + ".");
        // Once user has arrived at the vehicle's location, notify the company
        microMobilityVehicle.notifyArrivalAtPickupLocation(); // triggers startRide()
    }*/


}
