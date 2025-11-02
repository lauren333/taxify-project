package services;
import actors.IUser;
import navigation.ILocation;

public class SilentService extends Service{ //new from sprint 4
    public SilentService(IUser user, ILocation pickup, ILocation dropoff){ 
        super(user,pickup,dropoff); 
    }

    @Override
    public String getServiceType(){ 
        return "Silent Service"; 
    }
}
