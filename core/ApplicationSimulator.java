package core;
import java.util.List;
//SPRINT 3: NEW CLASS

import actors.IUser;
import vehicles.IMicroMobilityVehicle;
import vehicles.IVehicle;

public class ApplicationSimulator implements IApplicationSimulator, IObserver {
    private ITaxiCompany company;
    private List<IUser> users;
    private List<IVehicle> vehicles;
    private List<IMicroMobilityVehicle> microMobilityVehicles; 
    
    //public ApplicationSimulator(ITaxiCompany company, List<IUser> users, List<IVehicle> vehicles) {

    public ApplicationSimulator(ITaxiCompany company, List<IUser> users, List<IVehicle> vehicles, List<IMicroMobilityVehicle> microMobilityVehicles) {
        this.company = company;
        this.users = users;
        this.vehicles = vehicles;
        this.microMobilityVehicles = microMobilityVehicles;
    }
    
    @Override
    public void show() { //GIVEN CODE SPRINT 3//   
        // show the status of the vehicles
        System.out.println("\n--" + this.company.getName() + " status Vehicles--");
        for (IVehicle vehicle : this.vehicles) {
            System.out.println(vehicle.toString());
        }   

        System.out.println("\n--" + this.company.getName() + " status Micro-Mobilities--");
        for (IMicroMobilityVehicle microMobilityVehicle : this.microMobilityVehicles) {
            System.out.println(microMobilityVehicle.toString());
        }     
    }
    
    @Override
    public void showStatistics() { //GIVEN CODE SPRINT 3//   
        // show the statistics of the company
        String s = "\n---" + this.company.getName() + " Vehicle statistics--";
        for (IVehicle vehicle : this.vehicles) {            
            s = s + "\n" +
            String.format("%-8s", vehicle.getClass().getSimpleName()) + 
            String.format("%2d", vehicle.getId()) +  "    " +
            String.format("%-22s", vehicle.getDriver().getFirstName() + " " + vehicle.getDriver().getLastName()) + " | " + 
            String.format("%2d", vehicle.getStatistics().getServices()) + " services | " +
            String.format("%-3d", vehicle.getStatistics().getDistance()) + " km | " +
            String.format("%5.1f", vehicle.getStatistics().getBilling()) + " EUR | " +
            String.format("%2d", vehicle.getStatistics().getReviews()) + " reviews | " +
            String.format("%4.2f", vehicle.getStatistics().getStars()) + " stars | ";
                }         
        System.out.println(s);   

        ///*
        String ss = "\n---" + this.company.getName() + " Micro-Mobility statistics--";
        for (IMicroMobilityVehicle mmVehicle : this.microMobilityVehicles) {
            ss += "\n" +
            String.format("%-16s", mmVehicle.getClass().getSimpleName()) + 
            String.format("%2d", mmVehicle.getId()) + " | " +   
            String.format("%2d", mmVehicle.getStatistics().getServices()) + " services | " +
            String.format("%-3d", mmVehicle.getStatistics().getDistance()) + " km | " +  
            String.format("%5.1f", mmVehicle.getStatistics().getBilling()) + " EUR | " +   
            String.format("%2d", mmVehicle.getStatistics().getReviews()) + " reviews | " +  
            String.format("%4.2f", mmVehicle.getStatistics().getStars()) + " stars | "; 
        }
        System.out.println(ss);  
        //*/     
    }    

    @Override
    public void update() {
        // move vehicles to their next location
        for (IVehicle vehicle : this.vehicles) {
            vehicle.move();
        }

        // Loop through micro-mobility vehicles (e.g., E-scooters, E-bikes)
        for (IMicroMobilityVehicle microMobilityVehicle : this.microMobilityVehicles) {
            microMobilityVehicle.move(); 
        } 
    }

    @Override
    public void requestService() {  //OUR CODE SPRINT 3//      
        // finds an available user and requests a service to the Taxi Company
        for (IUser user : this.users) { 
            if (user.getService() == true){ 
                continue; 
            }
            if (user.getPinkServiceEligibility() && ApplicationLibrary.rand() % 3 == 0){
                user.requestPinkService();
                break;
            } 
                          
            if (ApplicationLibrary.rand() % 5 == 0){
                 user.requestSilentService();
                 break;
             
           }
            if (ApplicationLibrary.rand() % 3 == 0){
                user.requestMicroMobilityService();
                break;
            } 
            user.requestService();
            break;
        }
    }
    
    @Override
    public int getTotalServices() { //GIVEN CODE SPRINT 3//      
        return this.company.getTotalServices();
    }
    
    @Override
    public void updateObserver(String message) { //GIVEN CODE SPRINT 3//   
        System.out.println(message);
    }
}