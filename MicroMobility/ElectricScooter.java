package project.taxify.MicroMobility;
import project.taxify.ILocation;
//import project.taxify.VehicleStatus;

public class ElectricScooter extends MicroMobilityVehicle {
    private static final double RATE = 0.15;

    public ElectricScooter(int id, ILocation location) {
        super(id, location);
    }

    @Override
    public void startRide() {
        if (this.ridecount >= 3) {
            System.err.println("Eletric Scooter " + this.id + " has reached 3 rides. Recharging before starting this ride.");
            this.chargeBattery();    // Recharge battery
            this.ridecount = 0;      // Reset ride count after charging
        }
        if (this.batteryLevel < 100.0) {
            System.err.println("Electric Scooter" + this.getId() + "will start ride after charging: Battery too low (" + this.batteryLevel + "%).");
            this.chargeBattery();
            return;
        }
        if (this.batteryLevel <= 25.0) {
            System.err.println("Electric Scooter" + this.getId() + "will start ride after charging: Battery too low (" + this.batteryLevel + "%).");
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
        return "Eletric Scooter " + super.toString();
    }
}
