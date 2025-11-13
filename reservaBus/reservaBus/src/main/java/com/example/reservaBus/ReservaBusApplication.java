package com.example.reservaBus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.example")
@EnableJpaRepositories(basePackages = "com.example.domain.repositories")
@EntityScan(basePackages = "com.example.domain.entities")
public class ReservaBusApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservaBusApplication.class, args);
	}

}
