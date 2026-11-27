/* 
    Serializes current state (of the buisness logic/ simulation) into JSON. 
    React frontend periodincally pools backend to fetch those snapshots and update the dashboard. 
*/

package server;

// Load in the the data for the simulation
import actors.IUser;
import core.TaxifySimulation;
import jakarta.annotation.PreDestroy;
import navigation.ILocation;
import org.springframework.stereotype.Service;
import services.IService;
import services.SharedService;
import vehicles.IMicroMobilityVehicle;
import vehicles.IVehicle;
import vehicles.VehicleStatus;
import statistics.IStatistics;

import java.util.ArrayList;
// Stores keys and values and it stores the order 
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map; 
// run tasks for you
import java.util.concurrent.Executors;
// run tasks at repeated times/ on schedule 
import java.util.concurrent.ScheduledExecutorService;
// access to handler to stop / pause scheduled simulation tasktask
import java.util.concurrent.ScheduledFuture;
// Tell scheduler how often to run (unit of time)
import java.util.concurrent.TimeUnit;

// Service: Handle internal simualtion request (preparing state date, starting, stopping etc.) 
// Service runs actual simulation logic 
@Service
public class SimulationService {
    // We only want one instance of the taxify simulation
    private final TaxifySimulation simulation = new TaxifySimulation();
    // Instance of a scheduele with one background thread (for sequential updates)
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    // Different threads see most up to date reference of the running task
    // ? to not define type, as we are not requesting data 
    private volatile ScheduledFuture<?> runningTask;
    // steps in the simulator
    private int tick = 0;


    // Check if simulation is still running (true if it is running)  
    private boolean isRunning() {
        ScheduledFuture<?> task = runningTask;
        return task != null && !task.isDone();
    }


    // Public so simulation controller can call it
    // Advance the simulation by one step
    public void step() {
        // synchronized: only one thread can touch the simulation at a time (scheduler and HTTP requests take turns)
        synchronized (simulation) {
            try {
                simulation.update();
                tick++;
                if (tick % 3 == 0) {            // new ride request every 3 ticks
                    simulation.requestService();
                }
            } catch (Exception exception) {
                exception.printStackTrace();    // otherwise an error silently kills the scheduled task
            }
        }
    }


    // Only one thread to access simulation at a time
    public synchronized void start(long ms) {
        // minimum 100 ms so the frontend can't overload the server
        long interval = Math.max(100, ms);      
        // check is simualtion is already running, if so stop it before starting again 
        // so that when we want to change simulation speed for ex. 
        if (runningTask != null){ 
            runningTask.cancel(false);
        }
        runningTask = scheduler.scheduleAtFixedRate(this::step, 0, interval, TimeUnit.MILLISECONDS);
    }


    public synchronized void pause() {
        if (runningTask != null){ 
            runningTask.cancel(false);
        }
    }


    // predestroy cleans up background schedueler
    // stop schedueled updates when the application is closing 
    @PreDestroy
    void shutdown() {
        scheduler.shutdownNow();
    }


    // Status of simulation
    // Mapping string, objects in JSON  
    public Map<String, Object> status() {
        synchronized (simulation) {
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("company", simulation.getCompanyName());
            json.put("totalServices", simulation.getTotalServices());
            json.put("running", isRunning());
            return json;
        }
    }
    

    // full snapshot of application: vehicles, micro vehicles, users, events, recent events 
    // JSON (linked hash map) an arrays per different class (vehciles, etc.) that hold different dictionaries (and some values) 
    // Instead of creating creating map for each vehicle we use helper vehicleJSON and pass it the information
    public Map<String, Object> state() {
        synchronized (simulation) {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("tick", tick);
            snapshot.put("running", isRunning());
            snapshot.put("totalServices", simulation.getTotalServices());

            List<Map<String, Object>> vehicles = new ArrayList<>();
            for (IVehicle vehicle : simulation.getVehicles()) {
                // -1 = vehicles have no battery
                Map<String, Object> vehicleData = vehicleJson(vehicle.getId(), vehicle.getClass().getSimpleName(),
                        vehicle.getLocation(), vehicle.getService(), vehicle.getStatus(),
                        vehicle.getDestination(), vehicle.isFree(), -1);
                addStats(vehicleData, vehicle.getStatistics());
                vehicles.add(vehicleData);
    
            }
            snapshot.put("vehicles", vehicles);

            List<Map<String, Object>> microVehicles = new ArrayList<>();
            for (IMicroMobilityVehicle microVehicle : simulation.getMicroMobilityVehicles()) {
                Map<String, Object> vehicleData = vehicleJson(microVehicle.getId(), microVehicle.getClass().getSimpleName(),
                        microVehicle.getLocation(), microVehicle.getService(), microVehicle.getStatus(),
                        microVehicle.getDestination(), microVehicle.isFree(), microVehicle.getBatteryLevel());
                addStats(vehicleData, microVehicle.getStatistics());
                microVehicles.add(vehicleData);
            }
            snapshot.put("microVehicles", microVehicles);

            List<Map<String, Object>> users = new ArrayList<>();
            for (IUser user : simulation.getUsers()) {
                Map<String, Object> userData = new LinkedHashMap<>();
                userData.put("id", user.getId());
                userData.put("name", user.getFirstName());
                userData.put("x", user.getLocation().getX());
                userData.put("y", user.getLocation().getY());
                users.add(userData);
            }
            snapshot.put("users", users);
            // turns the snapshot of events coming from backend into JSON format, to be passed to eventlog
            snapshot.put("events", new ArrayList<>(simulation.getRecentEvents()));
            return snapshot;
        }
    }

    // ---------- helpers ----------

    // one vehicle (taxi, shuttle, scooter or bike)
    // riders   = everyone attached to the ride, with "aboard" true/false
    // dropoffs = every drop-off point of the current ride
    // nextStopX/nextStopY = where the vehicle is heading right now
    private Map<String, Object> vehicleJson(int vehicleId, String vehicleType, ILocation currentLocation,
                                            IService service, VehicleStatus status, ILocation destination,
                                            boolean available, double batteryLevel) {
        boolean isShared = service instanceof SharedService;

        Map<String, Object> vehicleData = new LinkedHashMap<>();
        vehicleData.put("id", vehicleId);
        vehicleData.put("type", vehicleType);
        vehicleData.put("x", currentLocation.getX());
        vehicleData.put("y", currentLocation.getY());
        vehicleData.put("status", String.valueOf(status));
        vehicleData.put("available", available);
        vehicleData.put("busy", service != null);
        vehicleData.put("isShared", isShared);
        vehicleData.put("batteryLevel", batteryLevel);

        if (service == null) return vehicleData;      // idle: nothing more to add

        if (destination != null) {
            vehicleData.put("nextStopX", destination.getX());
            vehicleData.put("nextStopY", destination.getY());
        }
        vehicleData.put("heading", status == VehicleStatus.PICKUP ? "pickup" : "carrying");

        List<Map<String, Object>> riders = new ArrayList<>();
        List<Map<String, Object>> dropoffs = new ArrayList<>();

        // Handles shared service logic, where we might have 2 different dropoff locations and 2 users 
        if (isShared) {
            SharedService sharedService = (SharedService) service;
            // The primary user (and their destination) is only listed until their part of the ride is complete
            // Each vehicles is a ditionary, and when its service is not null, it also carries a riders and dropoffs as a list 
            // which can hold up to 2 entries for shared services, one for primary and secondary (for ex. 2 rides, 2 dropoffs until primary is droppedoff)
            if (!sharedService.isPrimaryComplete()) {
                riders.add(rider(sharedService.getPrimaryUser().getId(), sharedService.hasfirstUserPickedup()));
                dropoffs.add(dropoff(sharedService.getPrimaryUser().getId(), sharedService.getPrimaryDropoff()));
            }
            riders.add(rider(sharedService.getSecondaryUser().getId(), sharedService.bothUsersPickedUp));
            dropoffs.add(dropoff(sharedService.getSecondaryUser().getId(), sharedService.getSecondaryDropoff()));
        } else {
            // single rider: aboard once the vehicle is in the SERVICE phase
            riders.add(rider(service.getUser().getId(), status == VehicleStatus.SERVICE));
            dropoffs.add(dropoff(service.getUser().getId(), service.getDropoffLocation()));
        }

        vehicleData.put("riders", riders);
        vehicleData.put("dropoffs", dropoffs);
        return vehicleData;
    }

    // Helper function: Packages up rider and if aboard  
    // Aboard: is this rider in the vehicle yet
    // Primary riders flag flips true once first user is picked up, once dropped off user is removed of array
    private Map<String, Object> rider(int userId, boolean aboard) {
        Map<String, Object> riderData = new LinkedHashMap<>();
        riderData.put("userId", userId);
        riderData.put("aboard", aboard);
        return riderData;
    }

    // Helper function: Takes users id and location -> packages destination and user id 
    private Map<String, Object> dropoff(int userId, ILocation location) {
        Map<String, Object> dropoffData = new LinkedHashMap<>();
        dropoffData.put("userId", userId);
        dropoffData.put("x", location.getX());
        dropoffData.put("y", location.getY());
        return dropoffData;
    }

    // Helper to package Per-vehicle statistics, called in state()
    // Adds one vehicle's statistics (serviceCount, distanceKm, revenue, reviewCount, rating)
    // to that vehicle's JSON dictionary. Called once per vehicle, before the
    // dictionary is added to the vehicles or microVehicles list.
    private void addStats(Map<String, Object> vehicleData, IStatistics stats) {
        vehicleData.put("serviceCount", stats.getServices());
        vehicleData.put("distanceKm", stats.getDistance());
        vehicleData.put("revenue",  stats.getBilling());
        vehicleData.put("reviewCount",  stats.getReviews());
        // getStars() is NaN when there are no reviews yet, which would break the JSON
        vehicleData.put("rating", stats.getReviews() == 0 ? 0.0 : stats.getStars());
    }
}