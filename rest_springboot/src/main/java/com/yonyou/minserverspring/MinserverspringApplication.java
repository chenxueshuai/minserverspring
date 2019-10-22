package com.yonyou.minserverspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.yonyou.shuai")
public class MinserverspringApplication {

	public static void main(String[] args) {
		SpringApplication.run(MinserverspringApplication.class, args);
	}
}
