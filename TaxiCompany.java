package project.taxify;
import java.util.List;
//import project.taxify.MicroMobility.IMicroMobilityVehicle; //sprint 5
import java.util.ArrayList; //sprint 4
//import project.taxify.MicroMobility.*; //sprint 5

//SPRINT 3: NEW CLASS 

public class TaxiCompany implements ITaxiCompany, ISubject {
    private String name;
    private List<IUser> users;
    private List<IVehicle> vehicles;
    //private List<IMicroMobilityVehicle> microMobilityVehicles; //new from sprint 5
    private int totalServices;
    //IOBSERVER INTERFACE ENSURES ANY CLASSES IMPLEMENTENTING IT DEFINES THE updateObserver(string message) METHOD
    //WHY? IT ALLOWS DIFFERENT CLASSES TO RESPOND TO UPDATES IN THE TaxiCompany CLASS
    //taxi company is the subject and the vehciles are the observers
    private IObserver observer;
    
    public TaxiCompany(String name, List<IUser> users, List<IVehicle> vehicles) {

    //public TaxiCompany(String name, List<IUser> users, List<IVehicle> vehicles, List<IMicroMobilityVehicle> microMobilityVehicles) {
        this.name = name;
        this.users = users;
        this.vehicles = vehicles;  
        //this.microMobilityVehicles = microMobilityVehicles;    //new from sprint 5  
        this.totalServices = 0;
        
        for (IUser user : this.users) {
            user.setCompany(this);
        }
        for (IVehicle vehicle : this.vehicles) {
            vehicle.setCompany(this);
        }

        /* 
        for (IMicroMobilityVehicle microMobilityVehicle : this.microMobilityVehicles) {  //new from sprint 5
            microMobilityVehicle.setCompany(this);
        }
        */

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
        
    @Override
    public void arrivedAtPickupLocation(IVehicle vehicle) {
        // notify the observer a vehicle arrived at the pickup location
        notifyObserver(vehicle.getClass().getSimpleName() + " " + vehicle.getId() + " arrived at pickup location " + vehicle.getService().getUser().getId());
    }


    

    @Override
    public void arrivedAtDropoffLocation(IVehicle vehicle) {
        // a vehicle arrives at the drop-off location
        IService service = vehicle.getService();       
        int user = service.getUser().getId();
        int userIndex = findUserIndex(user);
       
        // the taxi company requests the user to rate the service, and updates its status
        this.users.get(userIndex).rateService(service);
        this.users.get(userIndex).setService(false);

        // update the counter of services    
        this.totalServices--;         
        notifyObserver(String.format("%-8s",vehicle.getClass().getSimpleName()) + vehicle.getId() + " drops off user " + user);         
    }
    
    /* 
    //NEW FOR MMVEHICLES SPRINT 5 

    public void arrivedAtPickupLocation2(IMicroMobilityVehicle microMobilityVehicle) {
        notifyObserver(microMobilityVehicle.getUser().getFirstName() + " has arrived at the pickup location for " + microMobilityVehicle.getClass().getSimpleName() + " " + microMobilityVehicle.getId());
    }

    public void arrivedAtDropoffLocation2(IMicroMobilityVehicle microMobilityVehicle) {
        IService service = microMobilityVehicle.getService();       
        int user = service.getUser().getId();
        int userIndex = findUserIndex(user);

        // Let the user rate the ride and end service
        this.users.get(userIndex).rateService(service);
        this.users.get(userIndex).setService(false);

        // update totalServices counter
        this.totalServices--;
        notifyObserver(microMobilityVehicle.getClass().getSimpleName() + " " + microMobilityVehicle.getId() + " parked by user " + user + " at location " + microMobilityVehicle.getLocation().toString());
    } */

        
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

    /* 
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
    } */

    //new from sprint 4
    private List<IVehicle> listOfFreeVehicles(List<IVehicle> vehicleList){
        List<IVehicle> freeVehicles = new ArrayList<IVehicle>();
        for (int i = 0; i < vehicleList.size(); i++){
            IVehicle vehicle  = vehicleList.get(i);
            if (vehicle.isFree()){ 
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
            if (Character.toUpperCase(vehicle.getDriver().getGender()) == 'F'){ 
                femaleDrivenVehicles.add(vehicle);
            }
        }
        return femaleDrivenVehicles;
    }

    private int closestVehicle(List<IVehicle> vehicleList, ILocation location) { 
        int currentClosestDistance = Integer.MAX_VALUE;
        int closestIndex = -1;
 
        if (vehicleList.isEmpty()) { // if list is empty
            throw new IllegalArgumentException("The list of vehicles is empty.");
        }
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
     /*
    private int closestMicroMobilityVehicle(List<IMicroMobilityVehicle> microMobilityVehicles, ILocation location) {
        int currentClosestDistance = Integer.MAX_VALUE;
        int closestIndex = -1;
    
        if (microMobilityVehicles.isEmpty()) { 
            throw new IllegalArgumentException("The list of micro-mobility vehicles is empty.");
        }
        for (int i = 0; i <microMobilityVehicles.size(); i++) {
            IMicroMobilityVehicle vehicle = microMobilityVehicles.get(i);
            int manhattanDistanceVehicletoLocation = ApplicationLibrary.distance(vehicle.getLocation(), location);
    
            if (manhattanDistanceVehicletoLocation < currentClosestDistance) {
                currentClosestDistance = manhattanDistanceVehicletoLocation;
                closestIndex = i;
            }
        }
        return closestIndex;
    }
    

        //NEW LOGIC FOR MM VEHICLES SPRINT 5
     
     
    public boolean provideMicroMobilityService(int userId, ILocation origin, ILocation destination){ 
        int requestingUserIndex = findUserIndex(userId);
        //IUser user = users.get(requestingUserIndex);

        // List of available micro-mobility vehicles
        List<IMicroMobilityVehicle> freeMicroMobilityVehicles = this.listOfFreeMicroMobilityVehicles(this.microMobilityVehicles);
        int closestMMVehicleIndex = closestMicroMobilityVehicle(freeMicroMobilityVehicles, origin);

        if (closestMMVehicleIndex == -1) {
            return false; // No available micro-mobility vehicle
        }

        if (closestMMVehicleIndex != -1) {   // If a vehicle is found, update the user status and create a service 
            this.users.get(requestingUserIndex).setService(true);
            IService service = RideServiceTypeFactory.createMicroMobilityService(this.users.get(requestingUserIndex), origin, destination);

            //assign and reserve MMvehicle 
            this.microMobilityVehicles.get(closestMMVehicleIndex).reserve(service); 

            notifyObserver("User " + this.users.get(requestingUserIndex).getId() + " requests a " + service.getServiceType() + " Service from " + service.toString() +
                            ", the ride is assigned to " + this.microMobilityVehicles.get(closestMMVehicleIndex).getClass().getSimpleName() +
                            " " + this.microMobilityVehicles.get(closestMMVehicleIndex).getId() + " at location " + this.microMobilityVehicles.get(closestMMVehicleIndex).getLocation().toString());

            // update the counter of services I NEED TO ADD LOGIC FOR THIS I ASSUME         
            this.totalServices++;            
            return true; 
        }
        return false; 
    }
     
    */ 
    //updated in sprint 4
    @Override
    public boolean provideService(int user, ILocation origin, ILocation destination) {
        int requestingUsersIndex = findUserIndex(user);  

        if (provideSharedService(user, origin, destination)){
            return true;
        }

        List<IVehicle> freeVehicles = this.listOfFreeVehicles(this.vehicles); //get list of free and find closest 
        int vehicleIndex = this.closestVehicle(freeVehicles, origin);      

        if (vehicleIndex == -1){  //if no free vehicle found
            return false;
        }
        if (vehicleIndex != -1) {   // If a vehicle is found, update the user status and create a servi    
            // update the user status
            this.users.get(requestingUsersIndex).setService(true);
            // create a service with the user, the pickup and the drop-off location
            IService service = RideServiceTypeFactory.createStandardService(this.users.get(requestingUsersIndex), origin, destination);
            // assign the new service to the vehicle           
            this.vehicles.get(vehicleIndex).pickService(service);            
            //updated in sprint 4 to specify type of service
            notifyObserver("User " + this.users.get(requestingUsersIndex).getId() + " requests a " + service.getServiceType() + " Service from " + service.toString() + ", the ride is assigned to " +
                           this.vehicles.get(vehicleIndex).getClass().getSimpleName() + " " + this.vehicles.get(vehicleIndex).getId() + " at location " +
                           this.vehicles.get(vehicleIndex).getLocation().toString());     
            // update the counter of services          
            this.totalServices++;           
            return true;
        } 
        return false;           
    }

    @Override //new from sprint 4
    public boolean providePinkService(int user, ILocation origin, ILocation destination) {
        int requestingUsersIndex = findUserIndex(user);// Find the user's index in the list
        int closestFreeFemaleIndex = this.closestVehicle(listOfFreeVehicles(listOfFemaleDrivenVehicles()), destination);

        if (closestFreeFemaleIndex == -1){ //// If no female vehicle is available, return false
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
        int vehicleIndex = this.closestVehicle(listOfFreeVehicles(this.vehicles), destination); //closest free vehicle 

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
    public boolean provideSharedService(int userId, ILocation origin, ILocation destination){
        int requestingUsersIndex = findUserIndex(userId); // Find the user's index in the list 
        IUser user = this.users.get(requestingUsersIndex); //get user 
        int closestVehicleIndex = closestVehicle(this.vehicles, origin); //get index of closest vehicle 
        if (closestVehicleIndex == -1){  //if no vehicle is found
            return false; 
        }
        IVehicle closestVehicle = this.vehicles.get(closestVehicleIndex);
        IService currentService = closestVehicle.getService();
        // Vehicle is not currently assigned to any service so cant "share" if no one is there in the first place
        if (currentService == null) {
            return false;
        }
         // is the vehicle is already sharing? then cant join because theyre already coupled
        if (closestVehicle.isSharing()) {
            return false;
        }
        //is the distance allowed
        if (ApplicationLibrary.distance(currentService.getDropoffLocation(), destination) > ApplicationLibrary.MAX_SHARED_DISTANCE){ 
            return false;
        }
        //check if currents wants to share, if not false
        IUser currentOccupiedUser = currentService.getUser();
        if (!currentOccupiedUser.isAllowedToShareFlag()) { 
            return false;
        } 
        // Create a new service and a shared service
        IService newService = RideServiceTypeFactory.createStandardService(this.users.get(requestingUsersIndex), origin, destination);
        SharedService sharedService = RideServiceTypeFactory.createSharedService(currentService, newService);
        // Mark the  service as started 

        // Allow for shared service if the vehicle is in service or picking someone up since
        //since when dropping someone off doesnt make it much of sharing 
        if (closestVehicle.getStatus() == VehicleStatus.PICKUP || closestVehicle.getStatus() == VehicleStatus.SERVICE) {
            sharedService.markSecondaryStarted(); // Mark the second journey  
        } else {
            return false; 
        }

        // Assign the shared service to the vehicle
        closestVehicle.pickService(sharedService);
        //mark user as in service, true
        this.users.get(requestingUsersIndex).setService(true);
        //update observer 
        notifyObserver("User " + currentOccupiedUser.getId() + " and " +  user.getId() + " are now sharing a ride. " + "From " + sharedService.toString() + ", the ride is assigned to " +
        this.vehicles.get(closestVehicleIndex).getClass().getSimpleName() + " " + this.vehicles.get(closestVehicleIndex).getId() + " at location " +
        this.vehicles.get(closestVehicleIndex).getLocation().toString());
        //update stats
        this.totalServices++;
        return true;
    }
    
}

