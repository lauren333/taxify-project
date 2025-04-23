package project.taxify.MicroMobility;
import project.taxify.ILocation;
import project.taxify.VehicleStatus;
 
public class ElectricScooter extends MicroMobilityVehicle {
    private static final double RATE = 0.15;

    public ElectricScooter(int id, ILocation location) {
        super(id, location);
    }

    @Override
    public void move() {
        // Scooter-specific move logic
        if (this.status != VehicleStatus.SERVICE) {
            return; // Simply do nothing if not in service
        }

        // Charge only if the vehicle is in PICKUP mode (reserved)
        if (this.status == VehicleStatus.PICKUP) {
            if (this.ridecount >= 3 || this.batteryLevel <= 25.0) {
                this.chargeBattery();  // Charge the battery
                System.err.println("Charging Battery, Scooters must be recharged after 3 rides, or when the battery is below or equal to 25%.");
            }

            // If the battery is not fully charged, charge it first
            if (this.batteryLevel < 100) {
                this.chargeBattery();  // Charge the battery to 100%
                System.err.println("Charging Battery, in order to book a scooter, the battery must be fully charged.");
            }
        }

        super.move();  // Call the parent move logic if conditions are satisfied
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
