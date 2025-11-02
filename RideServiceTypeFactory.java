/* 
    NEW FROM SPRINT 4: Factory design is the best option, Ive tried stratedgy and decorator but this is the most simple and 
    requires less files / interfaces
        -I decided not to use a factory or decoratory because I dont want to pass in a stratedy or encapulate/wrap an object
        -I feel like these classes are distinct enough that it doesnt make since to pass in behavior/stratedgy at run time 

        -BENIFITS
        -Requires fewer files + interfaces, the implementation is simple 
        -Were CREATING different types of services / objects key word: CREATING so factory is a good option 
        -We can set different services to have distinct logic /critera on wether or not they can be created (pink for ex)
        -The factory design pattern allows me to easily add different service types down the line (open/closed principle)
        -Its centralizes the object creation, simple 
        -Behavior is related to the TYPE of service, and that’s something we check at the time of creation-time
            -NOT AT RUN TIME, WE DEFINE THE BEHAVIOR OF WETHER THE OBJECT CAN BE CREATED OR NOT BEFORE OBJECT EXISTS
                -CONTROLS HOW AND WHEN AN OBJECT IS CREATED 
 */

public class RideServiceTypeFactory {
    public static IService createStandardService(IUser user, ILocation pickup, ILocation dropoff){
        return new Service(user, pickup, dropoff);
    }
    public static IService createSilentService(IUser user, ILocation pickup, ILocation dropoff){
        return new SilentService(user, pickup, dropoff);
    }
    public static SharedService createSharedService(IService service1, IService service2){
        return new SharedService(service1, service2);
    }
    public static IService createPinkService(IUser user, ILocation pickup, ILocation dropoff){
        if (!user.getPinkServiceEligibility()) //check if user is women OR child before creating object
            throw new IllegalArgumentException("Pink Ride are Reserved for Women or Children under 18 Years of Age. Try our Silent or Regular Service, Thanks!");
        return new PinkService(user, pickup, dropoff);
    }
    // New method to create services for MicroMobility SPRINT 5
    public static IService createMicroMobilityService(IUser user, ILocation pickup, ILocation dropoff) {
        return new MicroMobilityService(user, pickup, dropoff);
    }
}