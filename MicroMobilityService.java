package project.taxify;

public class MicroMobilityService extends Service {
    public MicroMobilityService(IUser user, ILocation pickup, ILocation dropoff) {
        super(user,pickup,dropoff);
    }

    @Override
    public String getServiceType() {
        return "Micro-Mobility Service";
    }
}
