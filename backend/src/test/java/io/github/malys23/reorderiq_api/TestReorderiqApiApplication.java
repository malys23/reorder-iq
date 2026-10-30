package io.github.malys23.reorderiq_api;

import org.springframework.boot.SpringApplication;

public class TestReorderiqApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(ReorderiqApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
