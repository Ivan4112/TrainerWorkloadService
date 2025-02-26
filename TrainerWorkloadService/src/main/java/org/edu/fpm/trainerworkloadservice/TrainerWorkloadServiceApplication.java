package org.edu.fpm.trainerworkloadservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class TrainerWorkloadServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrainerWorkloadServiceApplication.class, args);
	}

}
