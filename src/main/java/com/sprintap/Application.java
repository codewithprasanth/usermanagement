package com.sprintap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot Application
 * Entry point for all modules: usermanagement, doarules, etc.
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = {
	"com.sprintap.doarules.repository",
	"com.sprintap.usermanagement.repository"
})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}

