package co.eci.blacklist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Main Spring Boot application class for the Blacklist API service.
 *
 * @author ARSW-PANDILLA-2025
 * @version 1.0
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class BlacklistApiApplication {
    
    /**
     * Main method to start the Spring Boot application.
     *
     * @param args Command line arguments passed to the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(BlacklistApiApplication.class, args);
    }
}
