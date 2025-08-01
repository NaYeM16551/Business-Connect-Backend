package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableScheduling
public class DemoApplication {

	public static void main(String[] args) {

		Dotenv dotenv = Dotenv.load();

		// load values into system environment
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
		SpringApplication.run(DemoApplication.class, args);
	}

}