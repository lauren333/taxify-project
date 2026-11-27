/*
    Starts springboot and embedded web server.
*/

package server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication searches server package for classes it should manage (@Service, @RestController) 
@SpringBootApplication
public class TaxifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaxifyApplication.class, args);
    }
}