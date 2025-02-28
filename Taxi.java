package project.taxify;

public class Taxi extends Vehicle { //New subclass from Sprint 2  
    private static final double RATE = 2.0; // 2 euros per block

    public Taxi(int id, ILocation location) {
        super(id, location);
    }

    @Override
    public double calculateCost() {
        return (double) (super.getService().calculateDistance() * RATE); 
    }

    @Override
    public String toString() { //override printing of function toString parent class, to include Taxi at the begining. 
        return "Taxi " + super.toString();
    }
}
