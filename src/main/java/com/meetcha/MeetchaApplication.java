package com.meetcha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MeetchaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MeetchaApplication.class, args);
	}

}
