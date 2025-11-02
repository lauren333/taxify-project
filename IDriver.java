import java.time.LocalDate;

//new from sprint 4
public interface IDriver {
    public String getFirstName();
    public String getLastName();
    public char getGender();
    public LocalDate getBirthday();
    public int getYearsOfExperience();
    public double getRating();
    public void setVehicle(IVehicle vehicle);
    public IVehicle getVehicle(); 
}