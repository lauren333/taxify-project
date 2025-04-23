package project.taxify.MicroMobility;
import project.taxify.ILocation;
import project.taxify.VehicleStatus;

public class ElectricScooter extends MicroMobilityVehicle {
    private static final double RATE = 0.15;

    public ElectricScooter(int id, ILocation location) {
        super(id, location);
    }

    @Override
    public void startRide() {
        if (this.ridecount >= 3) {
            System.err.println("Scooter " + this.id + " has reached 3 rides. Recharging before starting this ride.");
            this.chargeBattery();    // Recharge battery
            this.ridecount = 0;      // Reset ride count after charging
            // Prevent ride until re-reserved
        }
        if (this.batteryLevel < 100.0) {
            System.err.println("Will start ride after charging: Battery too low (" + this.batteryLevel + "%). Please charge.");
            this.chargeBattery();
            return;
        }
        if (this.batteryLevel <= 25.0) {
            System.err.println("Will start ride after charging: Battery too low (" + this.batteryLevel + "%). Please charge.");
            this.chargeBattery();
        }
        
    
        super.startRide(); // Proceed with ride
    }
    
    public void move() {
        // Handle PICKUP status (user walking to vehicle)
        if (this.status == VehicleStatus.PICKUP) {
            if (this.route.hasLocations()) {
                // Simulate user approaching the vehicle
                System.out.println(service.getUser().getFirstName() + ", User " + service.getUser().getId() + ", is walking to micro-mobility ElectricScooter " + this.id);
                this.route.getNextLocation(); // just progress the route
            } else {
                System.out.println(service.getUser().getFirstName() + ", User " + service.getUser().getId() + ", has arrived at micro-mobility ElectricScooter " + this.id);
                notifyArrivalAtPickupLocation(); // triggers startRide()
            }
            return; // don’t proceed to SERVICE behavior yet
        }
        // SERVICE: vehicle is in ride to drop-off
        if (this.status == VehicleStatus.SERVICE) {
            if (this.route.hasLocations()) {
                this.location = this.route.getNextLocation();
                System.out.println("ElectricScooter " + this.id + " driven by" + service.getUser().getFirstName() + ", User " + service.getUser().getId() + ", to: " + this.location);
            }

            if (!this.route.hasLocations()) {
                notifyArrivalAtDropoffLocation();
            }
        }
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
