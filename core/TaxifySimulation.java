package core;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import actors.Driver;
import actors.IDriver;
import actors.IUser;
import actors.User;
import vehicles.ElectricBike;
import vehicles.ElectricScooter;
import vehicles.IMicroMobilityVehicle;
import vehicles.IVehicle;
import vehicles.Shuttle;
import vehicles.Taxi;

// Everything the web dashboard needs lives here
// This class also acts as the observer of the TaxiCompany: it prints each message
// to the console AND keeps a newest-first feed for the dashboard.
public class TaxifySimulation implements IObserver {
    private List<IUser> users;
    private List<IVehicle> vehicles;
    private List<IMicroMobilityVehicle> microMobilityVehicles;

    private TaxiCompany company;
    private ApplicationSimulator simulator;

    // the updates in console
    private final LinkedList<Map<String, Object>> events = new LinkedList<>();
    private int nextEventId = 0;   // gives each dashboard event a unique id

    public TaxifySimulation() {

        // 1. Create users
        users = new ArrayList<IUser>();
        users.add(new User(1, "John", "Smith", 'M', LocalDate.of(1968, 9, 13), ApplicationLibrary.randomLocation()));
        users.add(new User(2, "Leonardo", "DiCaprio", 'M', LocalDate.of(1974, 11, 11), ApplicationLibrary.randomLocation()));
        users.add(new User(3, "Will", "Smith", 'M', LocalDate.of(1968, 9, 25), ApplicationLibrary.randomLocation()));
        users.add(new User(4, "Angelina", "Jolie", 'F', LocalDate.of(1975, 6, 4), ApplicationLibrary.randomLocation()));
        users.add(new User(5, "Christian", "Bale", 'M', LocalDate.of(1974, 1, 30), ApplicationLibrary.randomLocation()));
        users.add(new User(6, "Frank", "Sinatra", 'M', LocalDate.of(1915, 12, 12), ApplicationLibrary.randomLocation()));
        users.add(new User(7, "Nina", "Dobrev", 'F', LocalDate.of(1989, 1, 9), ApplicationLibrary.randomLocation()));
        users.add(new User(8, "Jackie", "Chan", 'M', LocalDate.of(1954, 4, 7), ApplicationLibrary.randomLocation()));
        users.add(new User(9, "Morgan", "Freeman", 'M', LocalDate.of(1937, 6, 1), ApplicationLibrary.randomLocation()));
        users.add(new User(10, "Audrey", "Hepburn", 'F', LocalDate.of(1929, 5, 4), ApplicationLibrary.randomLocation()));
        users.add(new User(11, "Lily-Rose", "Depp", 'F', LocalDate.of(1999, 5, 27), ApplicationLibrary.randomLocation()));
        users.add(new User(12, "Johnny", "Depp", 'M', LocalDate.of(1963, 6, 9), ApplicationLibrary.randomLocation()));
        users.add(new User(13, "Tom", "Cruise", 'M', LocalDate.of(1962, 7, 3), ApplicationLibrary.randomLocation()));
        users.add(new User(14, "Jennifer", "Lopez", 'F', LocalDate.of(1969, 7, 24), ApplicationLibrary.randomLocation()));
        users.add(new User(15, "Britney", "Spears", 'F', LocalDate.of(1981, 12, 2), ApplicationLibrary.randomLocation()));

        // 2. Create drivers
        List<IDriver> drivers = new ArrayList<IDriver>();

        drivers.add(new Driver("Lewis", "Hamilton", 'M', LocalDate.of(1985, 1, 7), 18));
        drivers.add(new Driver("Michael", "Schumacher", 'M', LocalDate.of(1969, 1, 3), 21));
        drivers.add(new Driver("Ayrton", "Senna", 'M', LocalDate.of(1960, 3, 21), 11));
        drivers.add(new Driver("Sebastian", "Vettel", 'M', LocalDate.of(1987, 7, 3), 15));
        drivers.add(new Driver("Max", "Verstappen", 'M', LocalDate.of(1997, 9, 30), 10));
        drivers.add(new Driver("Lella", "Lombardi", 'F', LocalDate.of(1941, 3, 26), 6));
        drivers.add(new Driver("Danica", "Patrick", 'F', LocalDate.of(1982, 3, 25), 14));
        drivers.add(new Driver("Susie", "Wolff", 'F', LocalDate.of(1982, 12, 6), 13));
        drivers.add(new Driver("Jamie", "Chadwick", 'F', LocalDate.of(1998, 5, 20), 8));
        drivers.add(new Driver("Michelle", "Mouton", 'F', LocalDate.of(1951, 6, 23), 15));

        // 3. Create taxis and shuttles
        vehicles = new ArrayList<IVehicle>();
        for (int i = 1; i <= 10; i++) {
            IVehicle vehicle;
            if (i % 4 == 0) {
                vehicle = new Shuttle(i, ApplicationLibrary.randomLocation());
            } else {
                vehicle = new Taxi(i, ApplicationLibrary.randomLocation());
            }
            IDriver driver = drivers.get(i - 1);
            vehicle.setDriver(driver);
            driver.setVehicle(vehicle);
            vehicles.add(vehicle);
        }

        // 4. Create electric bikes and scooters
        microMobilityVehicles = new ArrayList<IMicroMobilityVehicle>();
        for (int i = 1; i <= 10; i++) {
            IMicroMobilityVehicle vehicle;
            if (i % 4 == 0) {
                vehicle = new ElectricBike(i, ApplicationLibrary.randomLocation());
            } else {
                vehicle = new ElectricScooter(i, ApplicationLibrary.randomLocation());
            }
            microMobilityVehicles.add(vehicle);
        }

        // 5. Create the company and the application simulator
        // TaxiCompany manages the fleet and service assignments.
        company = new TaxiCompany("Taxify", users, vehicles, microMobilityVehicles);
        // ApplicationSimulator moves vehicles, requests new services and updates the simulation. 
        simulator = new ApplicationSimulator(company, users, vehicles, microMobilityVehicles);

        // 6. This class listens to the company (console + dashboard feed)
        company.addObserver(this);

        // 7. Request the initial five services
        for (int i = 0; i < 5; i++) {
            simulator.requestService();
        }
    }

    // simulation controls (pass-through to the original simulator) 

    public void update() {
        simulator.update();
    }

    public void requestService() {
        simulator.requestService();
    }

    public int getTotalServices() {
        return company.getTotalServices();
    }

    public String getCompanyName() {
        return company.getName();
    }

    // data for the web dashboard 

    public List<IUser> getUsers() {
        return users;
    }

    public List<IVehicle> getVehicles() {
        return vehicles;
    }

    public List<IMicroMobilityVehicle> getMicroMobilityVehicles() {
        return microMobilityVehicles;
    }

    public List<Map<String, Object>> getRecentEvents() {
        return events;
    }

    // observer: called by TaxiCompany for every event 
    // Taxicompany (the subject) sends updates on ride status to the observer (the simulator)
    // @Override
    // public void updateObserver(String message) {
    //     System.out.println(message);
    //     events.addFirst(message);                 // newest first
    //     if (events.size() > 30) events.removeLast();
    // }
        
    // observer: called by TaxiCompany for every event 
    @Override
    public void updateObserver(String message) {
        System.out.println(message);
    }

    // observer: called by TaxiCompany for requests and battery changes
    @Override
    public void recordEvent(Map<String, Object> event) {
        // copy event 
        Map<String, Object> storedEvent = new LinkedHashMap<>(event); 
        // give it a unique id
        storedEvent.put("id", nextEventId++);
        //newest at the top
        events.addFirst(storedEvent);  
        // keep 25 updates           
        if (events.size() > 25) events.removeLast();
    }
}    