package com.example.mwo_do_shil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MwoDoShilApplication {

	public static void main(String[] args) {
		SpringApplication.run(MwoDoShilApplication.class, args);
	}

}
