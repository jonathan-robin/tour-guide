package com.openclassrooms.tourguide;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = "com.openclassrooms.tourguide")
public class TourguideApplication {
	public static void main(String[] args) {
		SpringApplication.run(TourguideApplication.class, args);
	}

}
