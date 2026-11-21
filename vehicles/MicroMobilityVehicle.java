package vehicles;
import core.ApplicationLibrary;
import core.ITaxiCompany;
import navigation.ILocation;
import navigation.Route;
import services.IService;
import statistics.IStatistics;
import statistics.Statistics;

public abstract class MicroMobilityVehicle implements IMicroMobilityVehicle {
    protected static final double RESERVATION_FEE = 1.0;
    protected int id;
    protected core.ITaxiCompany company;
    protected services.IService service;
    protected VehicleStatus status;
    protected navigation.ILocation location;
    protected navigation.ILocation destination;
    protected statistics.IStatistics statistics;
    protected navigation.IRoute route;
    protected actors.IUser user;
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
            System.out.println(this.getClass().getSimpleName() + this.id + " fully charged.");
        } else {
            System.out.println(this.getClass().getSimpleName() + this.id + " is already full.");
        }
    }    

    // Users can reserve their vehicle and move towards it (pickup status)
    public void reserve(IService service) {
        this.service = service; 
        //create a route but from the users current location, to the location of the vehicle
        this.destination = service.getPickupLocation();
        this.route = new Route(service.getUser().getLocation(), this.location); 
        reserved = true;
        status = VehicleStatus.PICKUP;
        // System.out.println("Reserved for " + service.getUser().getFirstName() + ". Your Fee for Reservation: " + RESERVATION_FEE + "€");
    }
  
    @Override
    public void startRide() {
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
        this.status = VehicleStatus.FREE;
        //no need for new route since cant self drive
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

        // Ensure battery doesn't go below 0%
        if (this.batteryLevel < 25) {
            this.batteryLevel = 0;
        } else {
            // Reduce battery level after the ride is complete
            this.batteryLevel -= 25;
        }
        System.out.println(this.getClass().getSimpleName() + " " + this.id + " battery reduced after ride. Current battery level: " + this.batteryLevel + "%");
        // Ensure the user's location is updated to the destination when the ride ends
    }

    @Override
    public void move() {
        // Handle PICKUP status (user walking to vehicle)
        if (this.status == VehicleStatus.PICKUP) {
            if (this.route.hasLocations()) { 
                // get next location (route consumes it after getting it)
                ILocation usersNextStep = this.route.getNextLocation(); 
                service.getUser().setLocation(usersNextStep);
                System.out.println(service.getUser().getFirstName() + ", User " + service.getUser().getId()
                + ", is walking to micro-mobility vehicle " + this.getClass().getSimpleName() + " " + this.id
                + ", now at " + service.getUser().getLocation()
                + ", " + ApplicationLibrary.distance(service.getUser().getLocation(), this.location) + " blocks away");
                } 
            // If it does not have more locations, user has arrived
            if (!this.route.hasLocations()){  
                notifyArrivalAtPickupLocation(); // triggers startRide()
            }
            return; // don’t proceed to SERVICE behavior yet
        }
        // SERVICE: vehicle is in ride to drop-off
        if (this.status == VehicleStatus.SERVICE) {
            if (this.route.hasLocations()) {
                this.location = this.route.getNextLocation();
                service.getUser().setLocation(this.location);
                //System.out.println("Vehicle " + this.id + " driven by " + service.getUser().getFirstName() + ", User " + service.getUser().getId() + ", moving to: " + this.location);
            }
            // If there is no more locations we have arrived, + user is already at the location
            if (!this.route.hasLocations()) {
                if (this.location.getX() == destination.getX() && this.location.getY() == destination.getY()) {
                    service.getUser().setLocation(this.location);
                    notifyArrivalAtDropoffLocation();
                } 
            }
        }
        // FREE: do nothing
        /*  
        // If we have reached the destination 
        if (!this.route.hasLocations()) {
            ILocation dropoff = this.service.getDropoffLocation();
            if (this.location.getX() == dropoff.getX() && this.location.getY() == dropoff.getY()) {
                notifyArrivalAtDropoffLocation();
            }
        } */
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
        ? this.id + ": " + service.getUser().getFirstName() + " " + service.getUser().getLastName() + ", User " + service.getUser().getId() + ", at " + this.location + " is driving themself to " + this.destination
        : (this.status == VehicleStatus.PICKUP)
            ? this.id + ": reserved for user " + service.getUser().getId() + ", " + service.getUser().getFirstName() + " " + service.getUser().getLastName()
            : this.id + ": vehicle is idle at " + this.location;
    }   

}