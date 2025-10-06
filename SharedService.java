package project.taxify;

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
            return Math.abs(secondaryService.getPickupLocation().getX() - secondaryService.getDropoffLocation().getX()) 
                + Math.abs(secondaryService.getPickupLocation().getY() - secondaryService.getDropoffLocation().getY()) / 1;
        } else { 
            return Math.abs(primaryService.getPickupLocation().getX() - primaryService.getDropoffLocation().getX()) 
                + Math.abs(primaryService.getPickupLocation().getY() - primaryService.getDropoffLocation().getY()) / 1;
        }
    }

    @Override
    public IUser getUser(){ //is the first drop off complete?  
        return this.finishedPrimary ? this.secondaryService.getUser() : this.primaryService.getUser();
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
        return this.hasfirstUserPickedup() ? secondaryService.getPickupLocation() : primaryService.getPickupLocation();
    }

    public ILocation getDropoffLocation(){
        return this.finishedPrimary ? secondaryService.getDropoffLocation() : primaryService.getDropoffLocation();
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
        if (hasfirstUserPickedup()) {
            return secondaryService.getPickupLocation().toString() + " to " +
                   primaryService.getDropoffLocation().toString() + " and " +
                   secondaryService.getDropoffLocation().toString();
        } else {
            return primaryService.getPickupLocation().toString() + " and " +
                   secondaryService.getPickupLocation().toString() + " to " +
                   primaryService.getDropoffLocation().toString() + " and " +
                   secondaryService.getDropoffLocation().toString();
        }
    }
}
