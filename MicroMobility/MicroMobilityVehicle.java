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
     
    //@Override
    //public void setLocation(ILocation location) {
       // this.location = location;
    //}

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

    /* 
    public void setUser(IUser user){
        this.user = user; 
    }
    */
    
    public IUser getUser(){
        return this.user; 
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
        //this.destination = service.getPickupLocation(); 
        //this.route = new Route(this.location,this.destination); 
        this.user = service.getUser(); // Set the user when the vehicle is reserved
        reserved = true;
        status = VehicleStatus.PICKUP;
        System.out.println("Reserved. Your Fee for Reservation: " + RESERVATION_FEE + "€");

        // User should walk to the vehicle after reservation
        //or aactually the moving of the vehicle for the other class is done in the appliation simulator, and it moves the vehicle to the next space
        //we need to create a move for mobility vehicles and mayve see how we can make sure the user walks to there ride first 
        //this.user.walkToMicroMobilityVehicle(this); // This triggers the walking process 
        //jk i moved it into the update() section
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
        // Ensure the user's location is updated to the destination when the ride ends
        this.user.setLocation(this.service.getDropoffLocation()); 
    }
  
    public void move() {
        // Micromobility vehicles only move when they are being actively ridden/ in service 
        if (this.status != VehicleStatus.SERVICE) {
            return; //do nothing stay in place
            //throw new IllegalStateException("Micro-mobility vehicles can only move when in service.");
        }

        // Move to the next location in the route toward drop-off, get next location
        this.location = this.route.getNextLocation();
        // Reduce the battery level as the vehicle moves
        this.batteryLevel -= 25;
        // Sync user's location with the vehicle's location
        this.user.setLocation(this.location);  

        if (!this.route.hasLocations()) {
            if (this.service == null) {
                //the vehcile stays in place
                return; //do nothing
            }
            else {
                // check if the user has arrived to a pickup, or the vehicle to the drop off location
                IUser user = this.service.getUser(); 
                //ILocation origin = this.service.getPickupLocation();
                ILocation destination = this.service.getDropoffLocation();
                if (user.hasArrivedAtVehicle(this)) {
                    //System.out.println(this.id + "Arrived at Pickup Location");
                    notifyArrivalAtPickupLocation();
                } else if (this.location.getX() == destination.getX() && this.location.getY() == destination.getY()) {
                    //System.out.println(this.id + "Arrived at Dropoff Location");
                    notifyArrivalAtDropoffLocation();
                }        
            }
        }

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
        return this.id + ": " + user.getFirstName() + " " + user.getLastName() + " at " + this.location + " driving to " + this.destination +
               ((this.status == VehicleStatus.FREE) ? " is free at " + this.route.toString(): ((this.status == VehicleStatus.PICKUP) ?
               " is reserved for the user " + this.service.getUser().getId() : " in service "));
    }   

}
