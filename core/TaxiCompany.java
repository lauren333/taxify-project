package core;
import java.util.List;

import actors.IUser;
import navigation.ILocation;
import services.IService;
import services.RideServiceTypeFactory;
import services.SharedService;
import vehicles.IMicroMobilityVehicle;
import vehicles.IVehicle;
import vehicles.VehicleStatus;

import java.util.ArrayList; //sprint 4

//SPRINT 3: NEW CLASS 

public class TaxiCompany implements ITaxiCompany, ISubject {
    private String name;
    private List<IUser> users;
    private List<IVehicle> vehicles;
    private List<IMicroMobilityVehicle> microMobilityVehicles; //new from sprint 5
    private int totalServices;
    //IOBSERVER INTERFACE ENSURES ANY CLASSES IMPLEMENTENTING IT DEFINES THE updateObserver(string message) METHOD
    //IT ALLOWS DIFFERENT CLASSES TO RESPOND TO UPDATES IN THE TaxiCompany CLASS
    //taxi company is the subject and the vehciles are the observers
    private IObserver observer;
    
    //public TaxiCompany(String name, List<IUser> users, List<IVehicle> vehicles) {
    public TaxiCompany(String name, List<IUser> users, List<IVehicle> vehicles, List<IMicroMobilityVehicle> microMobilityVehicles) {
        this.name = name;
        this.users = users;
        this.vehicles = vehicles;  
        this.microMobilityVehicles = microMobilityVehicles;    //new from sprint 5  
        this.totalServices = 0;
        
        for (IUser user : this.users) {
            user.setCompany(this);
        }
        for (IVehicle vehicle : this.vehicles) {
            vehicle.setCompany(this);
        }

        ///* 
        for (IMicroMobilityVehicle microMobilityVehicle : this.microMobilityVehicles) {  //new from sprint 5
            microMobilityVehicle.setCompany(this);
        }
        //*/

    }
    
    @Override
    public String getName() {
        return this.name;
        // return name
    }

    @Override
    public int getTotalServices() {
        return this.totalServices;
        // return total services
    }
        
    // @Override
    // public void arrivedAtPickupLocation(IVehicle vehicle) {
    //     // notify the observer a vehicle arrived at the pickup location
    //     notifyObserver(vehicle.getClass().getSimpleName() + " " + vehicle.getId() + " arrived at pickup location " + vehicle.getService().getUser().getId());
    // }

    @Override
    public void arrivedAtPickupLocation(IVehicle vehicle) {
        IService service = vehicle.getService();
        IUser pickedUp = (service instanceof SharedService)
            ? ((SharedService) service).getPickupUser()
            : service.getUser();
        notifyObserver(vehicle.getClass().getSimpleName() + " " + vehicle.getId()
            + " arrived at pickup location " + pickedUp.getId());
    }

    @Override
    public void arrivedAtDropoffLocation(IVehicle vehicle) {
        // a vehicle arrives at the drop-off location
        IService service = vehicle.getService();  
        if (service == null)
        {
            System.out.println(vehicle.getStatus() + "ID: " + vehicle.getId());
            return;
        }            
        int user = service.getUser().getId();
        int userIndex = findUserIndex(user);
       
        // the taxi company requests the user to rate the service, and updates its status
        this.users.get(userIndex).rateService(service);
        this.users.get(userIndex).setService(false);

        // update the counter of services    
        this.totalServices--;         
        notifyObserver(String.format("%-8s",vehicle.getClass().getSimpleName()) + vehicle.getId() + " drops off user " + user);         
    }
    
    ///* 
    //NEW FOR MMVEHICLES SPRINT 5 

    @Override
    public void arrivedAtPickupLocation2(IMicroMobilityVehicle microMobilityVehicle) {
        IService service = microMobilityVehicle.getService(); 
        notifyObserver(service.getUser().getFirstName() + ", User " + service.getUser().getId() + ", has arrived at micro-mobility vehicle " + microMobilityVehicle.getClass().getSimpleName() + " " + microMobilityVehicle.getId());
    }
    
    @Override
    public void arrivedAtDropoffLocation2(IMicroMobilityVehicle microMobilityVehicle) {
        IService service = microMobilityVehicle.getService(); 
        
        if (service == null) {
            System.out.println("DEBUG: micro-mobility drop-off with no service, " + microMobilityVehicle.getClass().getSimpleName() + " ID: " + microMobilityVehicle.getId());
            return;
        } 
                 
        int user = service.getUser().getId();
        int userIndex = findUserIndex(user);
    
        // Let the user rate the ride and end service
        this.users.get(userIndex).rateService(service);
        this.users.get(userIndex).setService(false);
    
        // update totalServices counter
        this.totalServices--;
        notifyObserver(microMobilityVehicle.getClass().getSimpleName() + " " + microMobilityVehicle.getId() + " arrived at destination and is parked by " + service.getUser().getFirstName() + ", user "+ user + ", at location " + microMobilityVehicle.getLocation().toString());
    }
    
        
    @Override
    public void addObserver(IObserver observer) {
        // add a observer
        this.observer = observer; 
    }
    
    @Override
    public void notifyObserver(String message) {
        // send a message to the observer
        this.observer.updateObserver(message);
    }
    
    private int findUserIndex(int id) {
        // returns the index of the user id in this.users
        for (IUser user : this.users){ 
            if (user.getId() == id){ 
                return this.users.indexOf(user);
            }
        }
        return -1; //no id found 
    }

    private int findFreeVehicle() {
        // returns the index of a free vehicle in this.vehicles
        for (int i = 0; i < this.vehicles.size(); i++){
            if (this.vehicles.get(i).isFree())
                return i;
        }
        return -1;
    }

    //new from sprint 4
    private List<IVehicle> listOfFreeVehicles(List<IVehicle> vehicleList){
        List<IVehicle> freeVehicles = new ArrayList<IVehicle>();
        for (int i = 0; i < vehicleList.size(); i++){
            IVehicle vehicle  = vehicleList.get(i);
            if (vehicle.isFree()) { 
                freeVehicles.add(vehicle);
            }
        }
        return freeVehicles;
    }

     //new from sprint 4
    private List<IVehicle> listOfFemaleDrivenVehicles(){
        List<IVehicle> femaleDrivenVehicles = new ArrayList<IVehicle>();
        for (int i = 0; i < this.vehicles.size(); i++){
            IVehicle vehicle = this.vehicles.get(i);
            //if (Character.toUpperCase(vehicle.getDriver().getGender()) == 'F'){ 
            if (vehicle.getDriver().getGender() == 'F'){ 
                femaleDrivenVehicles.add(vehicle);
            }
        }
        return femaleDrivenVehicles;
    }


    ///* 
    //new from sprint 5 
    // Method to get a list of free micro-mobility vehicles
    private List<IMicroMobilityVehicle> listOfFreeMicroMobilityVehicles(List<IMicroMobilityVehicle> microMobilityVehicles) {
        List<IMicroMobilityVehicle> freeVehicles = new ArrayList<IMicroMobilityVehicle>();
        for (int i = 0; i < microMobilityVehicles.size(); i++) {
            IMicroMobilityVehicle vehicle = microMobilityVehicles.get(i);
            if (vehicle.isFree()) {
                freeVehicles.add(vehicle);
            }
        }
        return freeVehicles;
    } //*/


    private int closestVehicle(List<IVehicle> vehicleList, ILocation location) { 
        //int currentClosestDistance = Integer.MAX_VALUE;
        int currentClosestDistance = 9999;
        int closestIndex = -1;
        
        //if (vehicleList.isEmpty()) { // if list is empty
            //return -1;
            //throw new IllegalArgumentException("The list of vehicles is empty.");
        //}
        if (vehicleList.size() == 0)
            return -1; 

        for (int i = 0; i < vehicleList.size(); i++){ //goes through the whole list of vehicles!! to find closest 
            IVehicle vehicle = vehicleList.get(i);
            int manhattanDistanceVehicletoLocation = ApplicationLibrary.distance(vehicle.getLocation(), location);
            if (manhattanDistanceVehicletoLocation < currentClosestDistance){ //keep updating if we find a vehicle closer then previous
                currentClosestDistance = manhattanDistanceVehicletoLocation;
                closestIndex = this.vehicles.indexOf(vehicle);
            }
        }
        return closestIndex;
    }

    //NEW FOR SPRINT 5 
     ///*
    private int closestMicroMobilityVehicle(List<IMicroMobilityVehicle> microMobilityVehicles, ILocation location) {
        int currentClosestDistance = 9999;
        int closestIndex = -1;

        if (microMobilityVehicles.isEmpty()) { // if list is empty
            return -1; // Return -1 to indicate no vehicle found
        }

        //if (microMobilityVehicles.isEmpty()) { 
            //throw new IllegalArgumentException("The list of micro-mobility vehicles is empty.");
        //}s
        for (int i = 0; i <microMobilityVehicles.size(); i++) {
            IMicroMobilityVehicle vehicle = microMobilityVehicles.get(i);
            int manhattanDistanceVehicletoLocation = ApplicationLibrary.distance(vehicle.getLocation(), location);
    
            if (manhattanDistanceVehicletoLocation < currentClosestDistance) {
                currentClosestDistance = manhattanDistanceVehicletoLocation;
                closestIndex = i;
            }
        }
        IMicroMobilityVehicle mvehicle = microMobilityVehicles.get(closestIndex);
        return this.microMobilityVehicles.indexOf(mvehicle);
    }
    

        //NEW LOGIC FOR MM VEHICLES SPRINT 5
     
     
    @Override
    public boolean provideMicroMobilityService(int userId, ILocation origin, ILocation destination){ 
        
        int requestingUserIndex = findUserIndex(userId);
        IUser user = users.get(requestingUserIndex);

        // List of available micro-mobility vehicles
        List<IMicroMobilityVehicle> freeMicroMobilityVehicles = this.listOfFreeMicroMobilityVehicles(this.microMobilityVehicles);
        int closestMMVehicleIndex = closestMicroMobilityVehicle(freeMicroMobilityVehicles, origin);

        
        if (closestMMVehicleIndex == -1) {
            return false; // No available micro-mobility vehicle
        }
        user.setService(true);
        IService service = RideServiceTypeFactory.createMicroMobilityService(user, origin, destination);

        //assign and reserve MMvehicle 
        this.microMobilityVehicles.get(closestMMVehicleIndex).reserve(service); 

        notifyObserver("User " + this.users.get(requestingUserIndex).getId() + " requests a " + service.getServiceType() + " from " + service.toString() +
                            ", the ride is assigned to " + this.microMobilityVehicles.get(closestMMVehicleIndex).getClass().getSimpleName() +
                            " " + this.microMobilityVehicles.get(closestMMVehicleIndex).getId() + " at location " + this.microMobilityVehicles.get(closestMMVehicleIndex).getLocation().toString());

        // update the counter of services I NEED TO ADD LOGIC FOR THIS I ASSUME         
        this.totalServices++;            
        return true; 
    }
     
    private List<IVehicle> listOfOccupiedVehicles(){
        List<IVehicle> occupiedVehicleList = new ArrayList<IVehicle>();
        for (int i = 0; i < this.vehicles.size(); i++){
            IVehicle vehicle  = this.vehicles.get(i);
            if (!vehicle.isFree()){
                occupiedVehicleList.add(vehicle);
            }
        }
        return occupiedVehicleList;
    }

    private List<IVehicle> shareShameDropoff(ILocation location){
        List<IVehicle> occupiedVehicles = listOfOccupiedVehicles();
        List<IVehicle> sameDropOffList = new ArrayList<IVehicle>();
        for (IVehicle vehicle : occupiedVehicles){
            if (vehicle.getLocation().getX() == location.getX() && vehicle.getLocation().getY() == location.getY()){
                sameDropOffList.add(vehicle);
            }
        }
        return sameDropOffList;
    }

    //*/ 
    //updated in sprint 4
    @Override
    public boolean provideService(int user, ILocation origin, ILocation destination) {
        int requestingUsersIndex = findUserIndex(user); 
        
        
        int vehicleIndex = this.closestVehicle(this.vehicles, origin);      

        if (vehicleIndex == -1){  //if no free vehicle found
            return false;
        }

        IVehicle vehicle = this.vehicles.get(vehicleIndex);

        // Is the current closest vehicle is not free,
        // and the user is allowed to share (decided by random probability) 
        // and the vehicle is not already currently shared
        // then we can create a shared service

        // Provide shared service has two exits: true if all executed, false if the distance was too large to continue with the shared service. 
        IService current = vehicle.getService();
        if (!vehicle.isFree() && current.getServiceType().startsWith("Standard") && vehicle.getService().getUser().isAllowedToShareFlag() && !vehicle.isSharing()){
            if (provideSharedService(user, origin, destination, vehicle)) {
                return true;   // share worked, execute return true, we exit the method. 
            }
            // share was rejected (returned false): no return, so execution continues below
        }

        //if (provideSharedService(user, origin, destination)){
         //   return true;
        //}

        int freeVehicleIndex = this.closestVehicle(listOfFreeVehicles(this.vehicles), origin); //get list of free and find closest 
        if (freeVehicleIndex == -1){  //if no free vehicle found
            return false;
        }

        // update the user status
        this.users.get(requestingUsersIndex).setService(true);
            // create a service with the user, the pickup and the drop-off location
        IService service = RideServiceTypeFactory.createStandardService(this.users.get(requestingUsersIndex), origin, destination);
            // assign the new service to the vehicle           
        this.vehicles.get(freeVehicleIndex).pickService(service);            
            //updated in sprint 4 to specify type of service
        notifyObserver("User " + this.users.get(requestingUsersIndex).getId() + " requests a " + service.getServiceType()
            + " from " + service.toString() + ", the ride is assigned to "
            + this.vehicles.get(freeVehicleIndex).getClass().getSimpleName() + " "
            + this.vehicles.get(freeVehicleIndex).getId() + " at location "
            + this.vehicles.get(freeVehicleIndex).getLocation().toString());   
            // update the counter of services          
        this.totalServices++;           
        return true;          
    }

    @Override //new from sprint 4
    public boolean providePinkService(int user, ILocation origin, ILocation destination) {
        int requestingUsersIndex = findUserIndex(user);// Find the user's index in the list
        int closestFreeFemaleIndex = this.closestVehicle(listOfFreeVehicles(listOfFemaleDrivenVehicles()), origin);

        if (closestFreeFemaleIndex == -1){ //// If no female vehicle is available, return false
            notifyObserver("Unable to provide pink service");
            return false;
        } 

        // If a vehicle is found, update the user status and create a service  
        this.users.get(requestingUsersIndex).setService(true);
        IService pinkService = RideServiceTypeFactory.createPinkService(this.users.get(requestingUsersIndex), origin, destination);
        this.vehicles.get(closestFreeFemaleIndex).pickService(pinkService);
        //update observer
        notifyObserver("User " + this.users.get(requestingUsersIndex).getId() + " requests " + pinkService.getServiceType() + " from " + pinkService.toString() + ", the ride is assigned to " +
                            this.vehicles.get(closestFreeFemaleIndex).getClass().getSimpleName() + " " + this.vehicles.get(closestFreeFemaleIndex).getId() + " at location " +
                            this.vehicles.get(closestFreeFemaleIndex).getLocation().toString());
        // update the counter of services          
        this.totalServices++;
        return true;
    }
    
    @Override //new from sprint 4
    public boolean provideSilentService(int user, ILocation origin, ILocation destination) {
        int requestingUsersIndex = findUserIndex(user);// Find the user's index in the list
        int vehicleIndex = this.closestVehicle(listOfFreeVehicles(this.vehicles), origin); //closest free vehicle 

        if (vehicleIndex == -1){ //no vehicle is found
            return false;
        } 
        // If a vehicle is found, update the user status and create a service  
        this.users.get(requestingUsersIndex).setService(true);
        IService silentService = RideServiceTypeFactory.createSilentService(this.users.get(requestingUsersIndex), origin, destination); 
        this.vehicles.get(vehicleIndex).pickService(silentService);
        //update observer
        notifyObserver("User " + this.users.get(requestingUsersIndex).getId() + " requests " + silentService.getServiceType() + " from " + silentService.toString() + ", the ride is assigned to " +
                            this.vehicles.get(vehicleIndex).getClass().getSimpleName() + " " + this.vehicles.get(vehicleIndex).getId() + " at location " +
                            this.vehicles.get(vehicleIndex).getLocation().toString());
        //update services amount
        this.totalServices++;
        return true;
    }
    
    //new from sprint 4
    @Override
    public boolean provideSharedService(int userId, ILocation origin, ILocation destination, IVehicle vehicle){
        // get the new user, and exisiting service
        int userIndex = findUserIndex(userId);
        IUser user = this.users.get(userIndex);
        IService currentService = vehicle.getService(); 

        //check if the distance is allowed
        if (ApplicationLibrary.distance(currentService.getDropoffLocation(), destination) > ApplicationLibrary.MAX_SHARED_DISTANCE){ 
            return false;
        }

        // get current user in service
        IUser currentOccupiedUser = currentService.getUser();

        // Create a new service and a shared service
        IService newService = RideServiceTypeFactory.createStandardService(user, origin, destination);
        notifyObserver("User: " + user.getId() + " is requesting a shared ride from " + newService.toString());

        //combine services into shared service
        SharedService sharedService = RideServiceTypeFactory.createSharedService(currentService, newService);
        
        // Mark the  service as started 
        if (vehicle.getStatus() == VehicleStatus.SERVICE)
            sharedService.updateFirstUserPickedup();

        //assign shared service to vehicle
        vehicle.pickService(sharedService);

        // set new user to in service status
        user.setService(true);

        notifyObserver("User " + currentOccupiedUser.getId() + " accepted to share the ride and now " + "User " + currentOccupiedUser.getId() + " and " +  user.getId() + " are now sharing a ride. " + "From " + sharedService.toString() + ", the ride is assigned to " +
        vehicle.getClass().getSimpleName() + " " + vehicle.getId() + " at location " +
        vehicle.getLocation().toString());
        //update stats
        this.totalServices++;
        return true;
    }
    
}