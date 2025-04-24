package project.taxify.MicroMobility;
import project.taxify.ILocation;
//import project.taxify.VehicleStatus;


public class ElectricBike extends MicroMobilityVehicle {
    private static final double RATE = 0.25;

    public ElectricBike(int id, ILocation location) {
        super(id, location);
    }

    @Override
    public void startRide() {
        if (this.batteryLevel <= 25.0) {
            System.err.println("Electric Bike " + this.getId() + " will start ride after charging: Battery too low (" + this.batteryLevel + "%).");
            this.chargeBattery();
        }
        super.startRide(); // Proceed with ride
    }
    
    @Override
    public double calculateCost() {
        return RESERVATION_FEE + (super.calculateCost() * RATE); 
    }

    @Override
    public String toString() { //updated in sprint 4 to show whos driving 
        return "Electric Bike " + super.toString();
    }
}
