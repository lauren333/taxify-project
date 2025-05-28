package project.taxify;

public class SharedService implements IService { //new from sprint 4
    private IService primaryService;
    private IService secondaryService;
    private boolean isPrimaryComplete;
    private boolean hasStartedSecondary = false;
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
        if (isPrimaryComplete) { 
            return Math.abs(secondaryService.getPickupLocation().getX() - secondaryService.getDropoffLocation().getX()) 
                + Math.abs(secondaryService.getPickupLocation().getY() - secondaryService.getDropoffLocation().getY());
        } else { 
            return Math.abs(primaryService.getPickupLocation().getX() - primaryService.getDropoffLocation().getX()) 
                + Math.abs(primaryService.getPickupLocation().getY() - primaryService.getDropoffLocation().getY());
        }
    }

    @Override
    public IUser getUser(){ //is the first drop off complete?  
        return this.isPrimaryComplete ? this.secondaryService.getUser() : this.primaryService.getUser();
    }

    public void markPrimaryComplete() {
        this.isPrimaryComplete = true;
    }

    public void markSecondaryStarted() {
        this.hasStartedSecondary = true;
    } 

    public boolean isPrimaryComplete() {
        return isPrimaryComplete;
    }

    public boolean hasStartedSecondary() {
        return hasStartedSecondary;
    }

    public ILocation getPickupLocation(){
        return hasStartedSecondary ? secondaryService.getPickupLocation() : primaryService.getPickupLocation();
    }

    public ILocation getDropoffLocation(){
        return isPrimaryComplete ? secondaryService.getDropoffLocation() : primaryService.getDropoffLocation();
    }

    @Override
    public String getServiceType() {
        return "Shared Service";
    }

    @Override
    public void setStars(int stars){
        if (!hasStartedSecondary) {
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
        if (hasStartedSecondary) {
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
