package rgonzalez.smbc.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class Microservice {

	public static void main(String[] args) {
		SpringApplication.run(Microservice.class, args);
	}

}
