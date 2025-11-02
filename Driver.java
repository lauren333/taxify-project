import java.time.LocalDate;
 
//SPRINT 4: Define the interface IDriver and implement the class Driver to manage basic information about the driver’s profile
//ie. name, gender, birth date, years of experience, and overall rating.

public class Driver implements IDriver {
    private IVehicle vehicle; 
    private String firstName; 
    private String lastName; 
    private char gender; 
    private LocalDate birthday;  
    private int yearsOfExperience; 

    //getter and setters
    public Driver(String firstName, String lastName, char gender, LocalDate birthday, int yearsOfExperience){ 
        this.firstName = firstName; 
        this.lastName = lastName; 
        this.gender = gender; 
        this.birthday = birthday; 
        this.yearsOfExperience = yearsOfExperience; 
    }

    public String getFirstName(){ 
        return this.firstName; 
    }

    public String getLastName(){ 
        return this.lastName; 
    }

    public char getGender(){ 
        return this.gender; 
    }

    public LocalDate getBirthday(){ 
        return this.birthday; 
    }

    public int getYearsOfExperience(){ 
        return this.yearsOfExperience; 
    }
    
    public double getRating(){ 
        return this.vehicle.getStatistics().getStars(); 
    }

    public void setVehicle(IVehicle vehicle){ 
        this.vehicle = vehicle; 
    }

    public IVehicle getVehicle(){
        return this.vehicle;
    }
    
    @Override
    public String toString() { //just for printing in the test program 
        return this.firstName + " " + this.lastName + "  " + this.gender + "  " + this.birthday + " with " + this.yearsOfExperience + " years experience";
    } 
}

