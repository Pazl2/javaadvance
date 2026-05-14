package com.javaadvance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class JavaadvanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(JavaadvanceApplication.class, args);
	}

}
