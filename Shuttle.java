package project.taxify;

public class Shuttle extends Vehicle { //New subclass from Sprint 2 
    private static final double RATE = 1.5; // 1.5 euros per block

    public Shuttle(int id, ILocation location) {
        super(id, location);
    }

    @Override
    public double calculateCost() {
        return (double) (super.getService().calculateDistance() * RATE);
    }

        @Override
    public String toString() { //override printing of function toString parent class, to include Shuttle at the begining.  
        return "Shuttle " + super.toString();
    }

}
