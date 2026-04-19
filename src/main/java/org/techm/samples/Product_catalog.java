package org.techm.samples;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Product_catalog {

	static {
		// Load environment variables from .env file
		try {
			Dotenv dotenv = Dotenv.configure().load();
			dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
		} catch (Exception e) {
			// .env file might not exist or be readable, continue without it
			// This is normal for tests or CI/CD environments
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(Product_catalog.class, args);
	}
}