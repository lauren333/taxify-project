package project.taxify;

//new from sprint 4, defines logic for pink service.
//Factory checks eligibilioty of ride service type before creation of the object / service 
public class PinkService extends Service { 
    public PinkService(IUser user, ILocation pickup, ILocation dropoff){ 
        super(user,pickup,dropoff); 
    }
    
    @Override
    public String getServiceType(){
        return "Pink";
    }
}