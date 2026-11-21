package vehicles;
import java.util.List;
  
import actors.IDriver;
import core.ApplicationLibrary;
import core.ITaxiCompany;
import navigation.ILocation;
import navigation.IRoute;
import navigation.Route;
import services.IService;
import services.SharedService;
import statistics.IStatistics;
import statistics.Statistics;

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
    private List<IService> serviceList;

        
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

        if (isSharedService(this.service)){
            SharedService sharedService = (SharedService) this.service;
            if (!sharedService.hasfirstUserPickedup()){
                sharedService.updateFirstUserPickedup();
                this.destination = sharedService.getPickupLocation();
                this.route = new Route(this.location, destination);
                return;
            } else {
                sharedService.bothUsersPickedUp = true;
                this.destination = sharedService.getDropoffLocation();
                this.route = new Route(this.location, destination);
                this.status = VehicleStatus.SERVICE;
                return;
            } 
        } 
        this.destination = service.getDropoffLocation();

        //this.destination = service.getDropoffLocation(); 
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
        // this.company.arrivedAtDropoffLocation(this);

        if (isSharedService(this.service)){//new from sprint 4 updates, handle if shared service 
            SharedService sharedservice = (SharedService) this.service; //cast object into sharedservice to access its methods
            if(!sharedservice.isPrimaryComplete()){ //if the first dropoff is not marked complete 
                this.company.arrivedAtDropoffLocation(this);
                this.statistics.updateBilling(this.calculateCost());
                this.statistics.updateDistance(this.service.calculateDistance());
                this.statistics.updateServices();
                sharedservice.updateLeg();

                this.destination = sharedservice.getDropoffLocation(); 
                this.route = new Route(this.location, this.destination);
                if (sharedservice.AtLeastOneReviewedFlag){ //has at least one rating been done 
                    this.statistics.updateReviews();
                    this.statistics.updateStars(sharedservice.initialStars);
                    //sharedservice.AtLeastOneReviewedFlag = true; //flip the flag bc the first one rated
                }
                return; 
            } 
            this.company.arrivedAtDropoffLocation(this);
            this.endService(); 
        } else {
            this.company.arrivedAtDropoffLocation(this);
            this.endService(); //end the service 
        }


        //         sharedservice.markPrimaryComplete();
        //         this.destination = sharedservice.getDropoffLocation(); 
        //         this.route = new Route(this.location, this.destination);
        //         if (sharedservice.AtLeastOneReviewedFlag){ //has at least one rating been done 
        //             this.statistics.updateReviews();
        //             this.statistics.updateStars(sharedservice.initialStars);
        //             //sharedservice.AtLeastOneReviewedFlag = true; //flip the flag bc the first one rated
        //         }
        //         //this.startService(); //second part of ride
        //         return; //exit method, stats and reviews not updates yet
        //     }
        //     this.statistics.updateServices(); //update service stats for both rides,
        //     // total distance for both services is accumulated in the Statistics class, so only need to do it at the end
        //     //if (sharedservice.AtLeastOneReviewedFlag) //update the reviews for both accumulatively, or at least one ride
        //     //   this.statistics.updateReviews();
        //     this.company.arrivedAtDropoffLocation(this); 
        //     this.endService();
        // } else { 
        //     this.company.arrivedAtDropoffLocation(this);
        //     this.endService(); //end the service 
        // }
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

    private boolean isSharedService(IService service) {
        if (service instanceof SharedService)
            return true;
        return false;
    }

    @Override
    public void move() {
        // Move Vehicle to next location (wether or wether not it has a ride in service)
        if (this.route.hasLocations()){
                this.location = this.route.getNextLocation(); 
        }
        
        // Move the people inside of the vehcile if a service is in progress
        if (this.service != null){
            // Non shared service
            if (!isSharedService(this.service)){
                if (this.status == VehicleStatus.SERVICE){
                    this.service.getUser().setLocation(this.location);
                } 
            // Shared Service 
            } else {
                SharedService shared = (SharedService) this.service; 
                // If the first person is picked up and they havent been dropped off yet move their location 
                // (until primary trip aka dropping her off is complete) 
                if (shared.hasfirstUserPickedup() && !shared.isPrimaryComplete()) {
                    shared.getPrimaryUser().setLocation(this.location);
                }
                // and if both are picked up then move the second person too. 
                // this stops when we we move into the !haslocations case 
                if (shared.bothUsersPickedUp) {
                    shared.getSecondaryUser().setLocation(this.location);
                }
            }    
        }   
        // otherwise the vehicle has arrived to a pickup or drop off location
        if (!this.route.hasLocations()) {
            // If there is no active service, the vehicle can proceed on a random route
            if (this.service == null) {
                this.destination = ApplicationLibrary.randomLocation(this.location);
                this.route = new Route(this.location, this.destination);
            // Otherwise we need to pick up or drop off 
            } else {
                ILocation origin = this.service.getPickupLocation();
                ILocation destination = this.service.getDropoffLocation();
                // Shared Service Logic
                if (isSharedService(this.service)){
                    SharedService shared = (SharedService) this.service;
                    if (shared.bothUsersPickedUp && this.location.getX() == destination.getX() && this.location.getY() == destination.getY()){
                        notifyArrivalAtDropoffLocation();
                        return;
                    }
                }
                // Confirm if arrival or dropoff via coordinates (check this vecticals location and orgin/destination)
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
        if (isSharedService(this.service))
            return this.service.calculateDistance();
        return this.service.calculateDistance();
    }

    private int pickupUserId() {
        if (this.service instanceof SharedService)
            return ((SharedService) this.service).getPickupUser().getId();
        return this.service.getUser().getId();
    }

    @Override
    public String toString() {
        return this.id + ": " + driver.getFirstName().charAt(0) + ". " + driver.getLastName() + " at " + this.location + " driving to " + this.destination +
            ((this.status == VehicleStatus.FREE) ? " is free with path " + this.route.toString() : ((this.status == VehicleStatus.PICKUP) ?
            " to pickup user " + pickupUserId() : " in service "));
    }
    
}