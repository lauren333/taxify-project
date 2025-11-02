package services;

import actors.IUser;
import navigation.ILocation;

public class PinkService extends Service { 
    public PinkService(IUser user, ILocation pickup, ILocation dropoff){ 
        super(user,pickup,dropoff); 
    }
    
    @Override
    public String getServiceType(){
        return "Pink Service";
    }
}