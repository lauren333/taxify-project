package project.taxify;
import java.util.List;
//SPRINT 3: NEW CLASS

public class ApplicationSimulator implements IApplicationSimulator, IObserver {
    private ITaxiCompany company;
    private List<IUser> users;
    private List<IVehicle> vehicles;
    
    public ApplicationSimulator(ITaxiCompany company, List<IUser> users, List<IVehicle> vehicles) {
        this.company = company;
        this.users = users;
        this.vehicles = vehicles;
    }
    
    @Override
    public void show() { //GIVEN CODE SPRINT 3//   
        // show the status of the vehicles
        System.out.println("\n" + this.company.getName() + " status \n");
        for (IVehicle vehicle : this.vehicles) {
            System.out.println(vehicle.toString());
        }   
    }
    
    @Override
    public void showStatistics() { //GIVEN CODE SPRINT 3//   
        // show the statistics of the company
        String s = "\n" + this.company.getName() + " statistics \n";
        for (IVehicle vehicle : this.vehicles) {            
            s = s + "\n" +
            String.format("%-8s", vehicle.getClass().getSimpleName()) +
            String.format("%2s", vehicle.getId()) + " " +
            String.format("%2s", vehicle.getStatistics().getServices()) + " services " + 
            String.format("%3s", vehicle.getStatistics().getDistance()) + " km. " +
            String.format("%3s", vehicle.getStatistics().getBilling()) + " eur. " +
            String.format("%2s", vehicle.getStatistics().getReviews()) + " reviews " +
            String.format("%-4s", vehicle.getStatistics().getStars()) + " stars";
        }         
        System.out.println(s);        
    }    

    @Override
    public void update() {
        // move vehicles to their next location
        for (IVehicle vehicle : this.vehicles) {
               vehicle.move();
        }
    }

    @Override
    public void requestService() {  //OUR CODE SPRINT 3//      
        // finds an available user and requests a service to the Taxi Company
        for (IUser user : this.users) { 
            if (user.getService() == false){ 
                user.requestService();
                break;
            }
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
