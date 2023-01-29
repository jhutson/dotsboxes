package com.hutsondev.lobbyservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = { ReactiveUserDetailsServiceAutoConfiguration.class })
public class LobbyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LobbyServiceApplication.class, args);
	}

}
