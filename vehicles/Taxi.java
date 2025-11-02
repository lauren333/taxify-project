package vehicles;
import navigation.ILocation;
import services.SharedService;

public class Taxi extends Vehicle { //New subclass from Sprint 2  
    private static final double RATE = 2.0; // 2 euros per block

    public Taxi(int id, ILocation location) {
        super(id, location);
    }

    @Override
    public double calculateCost() {
        //UPDATED IN SPRINT 3// previously we recalculated the services distance, and then times the rate. 
        //but that logic is not good. Since we are in a subclass inhertating from the parent 
        //we should override and inheriet the calculate costfunction() from the parent class 
        //to reduce code instead of recalculating what calculatecost in super does
        if (getService() instanceof SharedService){
            return (super.calculateCost() * RATE) * 0.8; //20 Discount for shared rides 
        }
        return super.calculateCost() * RATE; 
    }

    @Override
    public String toString() { //override printing of function toString parent class, to include Taxi at the begining. 
        return "Taxi    " + super.toString();
    }
}
