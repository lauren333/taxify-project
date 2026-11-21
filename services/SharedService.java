package services;
import actors.IUser;
import navigation.ILocation;

public class SharedService implements IService { //new from sprint 4
    private IService primaryService;
    private IService secondaryService;
    private boolean finishedPrimary = false; 
    private boolean firstUserPickedup = false;
    public boolean bothUsersPickedUp = false;
    private int stars;
    public int initialStars = 0;
    public boolean AtLeastOneReviewedFlag = false;  //flag to be used in vehicle class 
    public boolean bothReviewed = false;


    public SharedService(IService primaryService, IService secondaryService){
        this.primaryService = primaryService;
        this.secondaryService = secondaryService;
        this.stars = 0;
    }

    public int calculateDistance() {
        if (finishedPrimary) { 
            return (Math.abs(this.secondaryService.getPickupLocation().getX() - this.secondaryService.getDropoffLocation().getX()) 
                + Math.abs(this.secondaryService.getPickupLocation().getY() - this.secondaryService.getDropoffLocation().getY()) ) / 1;
        } else { 
            return (Math.abs(this.primaryService.getPickupLocation().getX() - this.primaryService.getDropoffLocation().getX()) 
                + Math.abs(this.primaryService.getPickupLocation().getY() - this.primaryService.getDropoffLocation().getY()) ) / 1;
        }
    }

    @Override
    public IUser getUser(){ //is the first drop off complete?  
        if (!this.isPrimaryComplete())
            return this.primaryService.getUser();
        else
            return this.secondaryService.getUser();
    }

    public IUser getPrimaryUser() {
        return this.primaryService.getUser();
    }
    
    public IUser getSecondaryUser() {
        return this.secondaryService.getUser();
    }
    
    // Rider being picked up right now: primary first, then secondary
    public IUser getPickupUser() {
        if (this.hasfirstUserPickedup())
            return this.secondaryService.getUser();
        return this.primaryService.getUser();
    }

    public ILocation getPrimaryDropoff() {
        return this.primaryService.getDropoffLocation();
    }
    
    public ILocation getSecondaryDropoff() {
        return this.secondaryService.getDropoffLocation();
    }
    
    public void updateLeg() {
        this.finishedPrimary = true;
    }

    public void updateFirstUserPickedup(){
        this.firstUserPickedup = true; 
    }

    public boolean isPrimaryComplete() {
        return finishedPrimary;
    }

    public boolean hasfirstUserPickedup() {
        return firstUserPickedup;
    }

    public ILocation getPickupLocation(){
        if (this.hasfirstUserPickedup())
            return secondaryService.getPickupLocation();
        return primaryService.getPickupLocation();
    }

    public ILocation getDropoffLocation(){
        if (this.finishedPrimary)
            return secondaryService.getDropoffLocation();
        else
            return primaryService.getDropoffLocation();
    }

    @Override
    public String getServiceType() {
        return "Shared Service";
    }

    @Override
    public void setStars(int stars){
        if (!finishedPrimary) {
            this.initialStars = stars;
            this.AtLeastOneReviewedFlag = true;
            return;
        }
        this.stars += stars;
    }

    @Override
    public int getStars(){
        return this.stars;
    }

    public String toString(){
        if (this.hasfirstUserPickedup())
            return this.secondaryService.getPickupLocation().toString() + " to " + primaryService.getDropoffLocation().toString() + " and " + secondaryService.getDropoffLocation().toString();
        else
            return this.primaryService.getPickupLocation().toString() + " and " + this.secondaryService.getPickupLocation().toString() + " to " + primaryService.getDropoffLocation().toString() + " and " + secondaryService.getDropoffLocation().toString();
    }
}
