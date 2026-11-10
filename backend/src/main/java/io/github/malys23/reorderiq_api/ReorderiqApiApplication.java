package io.github.malys23.reorderiq_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ReorderiqApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReorderiqApiApplication.class, args);
	}

}
