package com.api.portaldatransparencia;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.api.portaldatransparencia.service.DatabaseInitializerService;

@SpringBootApplication
public class PortalDaTransparenciaApplication {

	@Autowired
	private DatabaseInitializerService databaseInitializerService;

	public static void main(String[] args) {
		SpringApplication.run(PortalDaTransparenciaApplication.class, args);
	}

	@Bean
	public CommandLineRunner initDatabase() {
		return args -> {
			databaseInitializerService.initializeDatabase();
		};
	}
}
