package project.taxify;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/* 
    // SPRINT 3: NEW CODE 

    The test program declares users and vehicles, these lists are shared by the taxi company (taxify) and the simulator (application). 

    TaxiCompany is part of ApplicationSimulator. This is why the instance of the class ApplicationSimulator gets the taxi company 
    as a parameter in the constructor method. ApplicationSimulator is an observer of TaxiCompany, the application is added using 
    addObserver().

    Declare a list of users. Instantiate at least 15 users.

    Declare a list of vehicles. Instantiate at least 10 vehicles (Taxis and Shuttles) and locate them at random locations of the city map.
    Instantiate the taxi company and the application simulator. Add the application simulator as an observer of the taxy company.
    The simulation begins with at least 5 requests of service, and ends when all vehicles end their services. 
    Show the status of the application each iteration and update its state. Simulate a request of a service randomly, 
    to avoid requesting a service each iteration.

    Finally, show the statistics.

    Statistics output: 
        Taxi     1  3 services  18 km. 36.0 eur.  1 reviews 4.0  stars
        Shuttle  2  4 services  30 km. 45.0 eur.  2 reviews 2.5  stars
        Taxi     3  3 services  22 km. 44.0 eur.  2 reviews 3.0  stars
        Taxi     4  4 services  30 km. 60.0 eur.  3 reviews 3.67 stars
        Taxi     5  4 services  38 km. 76.0 eur.  2 reviews 4.5  stars
        Shuttle  6  5 services  32 km. 48.0 eur.  1 reviews 5.0  stars
        Shuttle  7  7 services  41 km. 61.5 eur.  3 reviews 3.67 stars
        Taxi     8  3 services  23 km. 46.0 eur.  1 reviews 2.0  stars
        Taxi     9  5 services  26 km. 52.0 eur.  1 reviews 5.0  stars
        Taxi    10  5 services  41 km. 82.0 eur.  3 reviews 3.67 stars

 */

public class TestProgram {

    public static void main(String[] args) {
        //Declare a list of users.
        List<IUser> users = new ArrayList<IUser>();
        //Instantiate at least 15 users. (adding them to list "users")
        int usersize = 15;  
    
        users.add(new User(1, "John", "Smith", 'M', LocalDate.of(1968, 9, 13)));  
        users.add(new User(2, "Leonardo", "DiCaprio", 'M', LocalDate.of(1974, 11, 11)));   
        users.add(new User(3, "Will", "Smith", 'M', LocalDate.of(1968, 9, 25)));  
        users.add(new User(4, "Angelina", "Jolie", 'F', LocalDate.of(1975, 6, 4)));  
        users.add(new User(5, "Christian", "Bale", 'M', LocalDate.of(1974, 1, 30)));  
        users.add(new User(6, "Frank", "Sinatra", 'M', LocalDate.of(1915, 12, 12)));   
        users.add(new User(7, "Nina", "Dobrev", 'F', LocalDate.of(1989, 1, 9)));   
        users.add(new User(8, "Jackie", "Chan", 'M', LocalDate.of(1954, 4, 7))); 
        users.add(new User(9, "Morgan", "Freeman", 'M', LocalDate.of(1937, 6, 1)));  
        users.add(new User(10, "Audrey", "Hepburn", 'F', LocalDate.of(1929, 5, 4)));  
        users.add(new User(11, "Lily-Rose", "Depp", 'F', LocalDate.of(1999, 5, 27)));  
        users.add(new User(12, "Johnny", "Depp", 'M', LocalDate.of(1963, 6, 9)));  
        users.add(new User(13, "Tom", "Cruise", 'M', LocalDate.of(1962, 7, 3)));  
        users.add(new User(14, "Jennifer", "Lopez", 'F', LocalDate.of(1969, 7, 24)));  
        users.add(new User(15, "Britney", "Spears", 'F', LocalDate.of(1981, 12, 2)));  
                
        System.out.println("\n" + "--------TAXIFY--------");
        System.out.println("\n" + usersize + " Users Are Online: ");
        for (IUser user : users) {  
            System.out.println(user);
        }

        //Declare a list of vehicles. 
        List<IVehicle> vehicles = new ArrayList<IVehicle>();
        //Instantiate at least 10 vehicles (Taxis and Shuttles) and locate them at random locations of the city map.
        int vehiclesize = 10;  // OR WE CAN DO THIS: int vehiclesize = random.nextInt(10, 12);
        for (int i = 1; i <= vehiclesize; i++) {
            if (i % 4 == 0) { //every 4th vehicle is a shuttle
                vehicles.add(new Shuttle(i , ApplicationLibrary.randomLocation())); 
            } else {
                vehicles.add(new Taxi(i , ApplicationLibrary.randomLocation())); 
            }
        }

        System.out.println("\n" + vehiclesize + " Vehicles Are Online: ");
        for (IVehicle vehicle : vehicles) {  
            System.out.println(vehicle);
        }
        System.out.println("\n"); 

        // Instantiate the taxi company and the application simulator. 
        TaxiCompany taxicompany = new TaxiCompany("Taxify", users, vehicles);
        ApplicationSimulator appsimulator = new ApplicationSimulator(taxicompany, users, vehicles);
        //Add the application simulator as an observer of the taxy company.
        taxicompany.addObserver(appsimulator);

        // The simulation begins with at least 5 requests of service
        int start_servicesize = 5;
        Random random = new Random();

        // Request initial services
        for (int i = 0; i < start_servicesize; i++) {
            appsimulator.requestService();
        }

        // Simulate services and requests
        while (appsimulator.getTotalServices() > 0) { //ends when all vehicles end their services. 
            if (random.nextInt(100) < 25) { //Simulate a request of a service randomly, 25% chance, to avoid requesting a service each iteration
                appsimulator.requestService();
            }
            appsimulator.show(); //Show the status of the application each iteration
            System.out.println("Updates: ");
            appsimulator.update(); //and update its state 
        }

        //Finally, show the statistics.
        System.out.println();
        appsimulator.showStatistics();
    }
}
