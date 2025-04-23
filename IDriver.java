package project.taxify;

import java.time.LocalDate;

//new from sprint 4
public interface IDriver {
    String getFirstName();
    String getLastName();
    char getGender();
    LocalDate getBirthday();
    int getYearsOfExperience();
    double getRating();
    void setVehicle(IVehicle vehicle);
}