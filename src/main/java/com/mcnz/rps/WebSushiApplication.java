package com.mcnz.rps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.mcnz.rps")
public class WebSushiApplication {
	public static void main(String[] args) {
		SpringApplication.run(WebSushiApplication.class, args);
	}
}
