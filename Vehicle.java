package project.taxify;

import java.util.List;

public abstract class Vehicle implements IVehicle {
    private int id;
    private ITaxiCompany company;
    private IService service;
    private VehicleStatus status;
    private ILocation location;
    private ILocation destination;
    private IStatistics statistics;
    private IRoute route;
    private IDriver driver; //new from sprint 4
        
    public Vehicle(int id, ILocation location) {        
        this.id = id;
        this.service = null;
        this.status = VehicleStatus.FREE;
        this.location = location;        
        this.destination = ApplicationLibrary.randomLocation(this.location);
        this.statistics = new Statistics();
        this.route = new Route(this.location, this.destination);
    }

    @Override
    public int getId() { // NEW CODE Sp2 //
        return this.id; 
    }
 
    @Override
    public ILocation getLocation() { // NEW CODE Sp2 //
        return this.location; 
    }

    @Override
    public ILocation getDestination() { // NEW CODE Sp2 //
        return this.destination; 
    }
    
    @Override
    public IService getService() { // NEW CODE Sp2 //
        return this.service; 
    }
    
    @Override
    public IStatistics getStatistics() { // NEW CODE Sp2 //
        return this.statistics; 
    }
    
    @Override
    public void setCompany(ITaxiCompany company) {
        this.company = company;
    }

    @Override //new from sprint 4
    public VehicleStatus getStatus(){
        return this.status;
    }

    public void setDriver(IDriver driver){ //new from sprint 4
        this.driver = driver;
    }

    public IDriver getDriver(){ //new from sprint 4
        return this.driver; 
    }

    @Override
    // GIVEN CODE Sp2 // pick a service, set destination to the service pickup location, and status to "pickup"
    public void pickService(IService service) { 
        this.service = service;
        this.destination = service.getPickupLocation();
        this.route = new Route(this.location, this.destination);        
        this.status = VehicleStatus.PICKUP;
    }


    @Override
    // NEW CODE Sp2 // set destination to the service drop-off location, and status to "service"
    public void startService() {
        this.destination = service.getDropoffLocation(); 
        this.route = new Route(this.location, this.destination); 
        this.status = VehicleStatus.SERVICE; 
    }


    @Override
    public void endService() {
        // update vehicle statistics
        this.statistics.updateBilling(this.calculateCost());
        this.statistics.updateDistance(this.service.calculateDistance());
        this.statistics.updateServices();
        // if the service is rated by the user, update statistics
        if (this.service.getStars() != 0) {
            this.statistics.updateStars(this.service.getStars());
            this.statistics.updateReviews();
        }
        // set service to null, and status to "free"
        this.service = null;
        this.destination = ApplicationLibrary.randomLocation(this.location);
        this.route = new Route(this.location, this.destination);
        this.status = VehicleStatus.FREE;
    }


    @Override
    // NEW CODE Sp2 // notify the company that the vehicle is at the pickup location and start the service
    public void notifyArrivalAtPickupLocation() {
        //UPDATE SPRINT 3 // WE MUST HAVE THIS.COMPANY SINCE WE WANT THE CURRENT INSTANCE 
        this.company.arrivedAtPickupLocation(this); 
        this.startService();
    }
      
    
    @Override
    // NEW CODE Sp2 // notify the company that the vehicle is at the drop off location and end the service
    public void notifyArrivalAtDropoffLocation() {
        this.company.arrivedAtDropoffLocation(this);
        if (this.service instanceof SharedService){//new from sprint 4 updates, handle if shared service 
            SharedService sharedservice = (SharedService) this.service; //cast object into sharedservice to access its methods
            if(!sharedservice.isPrimaryComplete()){ //if the first dropoff is not marked complete 
                sharedservice.markPrimaryComplete(); //mark it completed 
                if (sharedservice.getStars() >= 1){ //has at least one rating been done 
                    sharedservice.AtLeastOneReviewedFlag = true; //flip the flag bc the first one rated
                }
                this.startService(); //second part of ride
                return; //exit method, stats and reviews not updates yet
            }
            this.statistics.updateServices(); //update service stats for both rides,
            // total distance for both services is accumulated in the Statistics class, so only need to do it at the end
            if (sharedservice.AtLeastOneReviewedFlag) //update the reviews for both accumulatively, or at least one ride
                this.statistics.updateReviews();
            this.endService();
        } else { 
            this.endService(); //end the service 
        }
    }
     
    
    @Override
    // NEW CODE Sp2 // returns true if the status of the vehicle is "free" and false otherwise
    public boolean isFree() {
        return this.status == VehicleStatus.FREE; 
    }   
 
    @Override //new from sprint 4
    public boolean isSharing(){
        return this.service instanceof SharedService ? true : false; 
    } 

    @Override
    public void move() {
        // get the next location from the driving route
        this.location = this.route.getNextLocation(); 
            
        // if the route has more locations the vehicle continues its route, otherwise the vehicle has arrived to a pickup or drop off location
        if (!this.route.hasLocations()) {
            if (this.service == null) {
                // the vehicle continues its random route
                this.destination = ApplicationLibrary.randomLocation(this.location);
                this.route = new Route(this.location, this.destination);
            } else {
                // check if the vehicle has arrived to a pickup or drop off location
                ILocation origin = this.service.getPickupLocation();
                ILocation destination = this.service.getDropoffLocation();
                if (this.location.getX() == origin.getX() && this.location.getY() == origin.getY()) {
                    //System.out.println(this.id + "Arrived at Pickup Location");
                    notifyArrivalAtPickupLocation();
                } else if (this.location.getX() == destination.getX() && this.location.getY() == destination.getY()) {
                    //System.out.println(this.id + "Arrived at Dropoff Location");
                    notifyArrivalAtDropoffLocation();
                }        
            }
        }
    }

    @Override
    public double calculateCost() {
        return this.service.calculateDistance();
    }

    @Override
    public String toString() { //updated in sprint 4 to show whos driving 
        return this.id + ": " + driver.getFirstName().charAt(0) + ". " + driver.getLastName() + " at " + this.location + " driving to " + this.destination +
               ((this.status == VehicleStatus.FREE) ? " is free with path " + this.route.toString(): ((this.status == VehicleStatus.PICKUP) ?
               " to pickup user " + this.service.getUser().getId() : " in service "));
    }   
    
}