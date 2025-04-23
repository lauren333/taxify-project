package project.taxify.MicroMobility;
import project.taxify.ApplicationLibrary;
import project.taxify.ILocation;
import project.taxify.IRoute;
import project.taxify.IService;
import project.taxify.Statistics;
import project.taxify.IStatistics;
import project.taxify.ITaxiCompany;
import project.taxify.IUser;
import project.taxify.Route;
import project.taxify.VehicleStatus;

public abstract class MicroMobilityVehicle implements IMicroMobilityVehicle {
    protected static final double RESERVATION_FEE = 1.0;
    protected int id;
    protected ITaxiCompany company;
    protected IService service;
    protected VehicleStatus status;
    protected ILocation location;
    protected ILocation destination;
    protected IStatistics statistics;
    protected IRoute route;

    protected IUser user;
    protected boolean reserved = false;
    protected double batteryLevel = 100.0; //start at 100% for all micromobility vehicles
    protected int ridecount = 0;
    

    public MicroMobilityVehicle(int id, ILocation location) {
        this.id = id;
        this.service = null; 
        this.status = VehicleStatus.FREE;
        this.location = location;
        this.destination = ApplicationLibrary.randomLocation(location);
        this.statistics = new Statistics();
        this.route = new Route(this.location, this.destination);
    }

    @Override
    public int getId() {
        return id;
    }
     
    @Override
    public void setLocation(ILocation location) {
       this.location = location;
    }

    @Override
    public ILocation getLocation() {  
        return this.location; 
    }

    @Override
    public ILocation getDestination() {  
        return this.destination; 
    }

    @Override
    public IService getService() { 
        return this.service; 
    }
    
    @Override
    public IStatistics getStatistics() { 
        return this.statistics; 
    }
    
    @Override
    public void setCompany(ITaxiCompany company) {
        this.company = company;
    }


    @Override
    public VehicleStatus getStatus() {
        return this.status; // FREE, PICKUP (means RESERVED), or IN_RIDE
    }

    @Override
    public double getBatteryLevel() {
        return this.batteryLevel;
    }

    @Override
    public void chargeBattery() {
        if (this.batteryLevel < 100) {
            this.batteryLevel = 100;
            System.out.println("Vehicle fully charged.");
        } else {
            System.out.println("Battery is already full.");
        }
    }    

    //pickup here means reserved
    public void reserve(IService service) {
        this.service = service; 
        //check whats getpickup location doing here
        //and creating route
        this.destination = service.getPickupLocation();
        this.route = new Route(this.destination,this.location); 
        reserved = true;
        status = VehicleStatus.PICKUP;
        //System.out.println("Reserved. Your Fee for Reservation: " + RESERVATION_FEE + "€");
    }
  
    //pickup here means reserved
    @Override
    public void startRide() {
        //if (status == VehicleStatus.SERVICE || status == VehicleStatus.PICKUP) { 
            //throw new IllegalStateException("Cannot start ride. Vehicle is in service.");
        //}
        //if (this.batteryLevel == 0){
            //throw new IllegalStateException("Cannot start ride. Vehicle not charged.");
        //}
        // Ensure the user has arrived at the vehicle before transitioning to service
       //if (!this.user.hasArrivedAtVehicle(this)) {
            //throw new IllegalStateException("User has not arrived at the vehicle yet. Cannot start the ride.");
        //}

        this.destination = service.getDropoffLocation(); 
        this.route = new Route(this.location, this.destination); 
        status = VehicleStatus.SERVICE;
    }
  

    @Override
    public void endRide() {
        this.statistics.updateBilling(this.calculateCost());
        this.statistics.updateDistance(this.service.calculateDistance());
        this.statistics.updateServices();
        this.ridecount++; //update ride count for MM vehcile 

        // if the service is rated by the user, update statistics
        if (this.service.getStars() != 0) {
            this.statistics.updateStars(this.service.getStars());
            this.statistics.updateReviews();
        }
        // set service to null, and status to "free"
        this.service = null;
        //this.destination = ApplicationLibrary.randomLocation(this.location);
        //this.route = new Route(this.location, this.destination);
        this.status = VehicleStatus.FREE;
        this.ridecount++; // ← THIS increments for this specific vehicle
    }
    
    @Override
    public void notifyArrivalAtPickupLocation() {
        this.company.arrivedAtPickupLocation2(this); 
        this.startRide(); 
    } 
    
    @Override
    public void notifyArrivalAtDropoffLocation() {
        this.company.arrivedAtDropoffLocation2(this);
        this.endRide();

        // Reduce battery level after the ride is complete
        this.batteryLevel -= 25;
        System.out.println("Battery reduced after ride. Current battery level: " + this.batteryLevel + "%");

        // Ensure battery doesn't go below 0%
        if (this.batteryLevel < 0) {
            this.batteryLevel = 0;
        } 
        // Ensure the user's location is updated to the destination when the ride ends
    }
  
    public void move() {
        // Handle PICKUP status (user walking to vehicle)
        if (this.status == VehicleStatus.PICKUP) {
            if (this.route.hasLocations()) {
                // Simulate user approaching the vehicle
                System.out.println(service.getUser().getFirstName() + ", User " + service.getUser().getId() + ", is walking to micro-mobility vehicle " + this.id);
                this.route.getNextLocation(); // just progress the route
            } else {
                System.out.println(service.getUser().getFirstName() + ", User " + service.getUser().getId() + ", has arrived at micro-mobility vehicle " + this.id);
                notifyArrivalAtPickupLocation(); // triggers startRide()
            }
            return; // don’t proceed to SERVICE behavior yet
        }
        // SERVICE: vehicle is in ride to drop-off
        if (this.status == VehicleStatus.SERVICE) {
            if (this.route.hasLocations()) {
                this.location = this.route.getNextLocation();
                System.out.println("Vehicle " + this.id + " driven by " + service.getUser().getFirstName() + ", User " + service.getUser().getId() + ", moving to: " + this.location);
            }

            if (!this.route.hasLocations()) {
                notifyArrivalAtDropoffLocation();
            }
        }

        // FREE: do nothing

        // If we have reached the destination 
        if (!this.route.hasLocations()) {
            ILocation dropoff = this.service.getDropoffLocation();
            if (this.location.getX() == dropoff.getX() && this.location.getY() == dropoff.getY()) {
                notifyArrivalAtDropoffLocation();
            }
        }
    }

    @Override
    public double calculateCost(){ 
       return this.service.calculateDistance(); 
    }

    @Override
    public boolean isFree() {
        return this.status == VehicleStatus.FREE; 
    }   
 
    @Override
    public String toString() { //updated in sprint 4 to show whos driving 
        return (this.status == VehicleStatus.SERVICE)
        ? this.id + ": " + service.getUser().getFirstName() + " " + service.getUser().getLastName() + " at " + this.location + " is driving themself to " + this.destination
        : (this.status == VehicleStatus.PICKUP)
            ? this.id + ": reserved for user " + service.getUser().getFirstName() + " " + service.getUser().getLastName()
            : this.id + ": vehicle is idle at " + this.location;
    }   

}
