package com.example.game_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing; // ✅ 이거 import도 필요

@SpringBootApplication
@EnableJpaAuditing  //  @CreatedDate, @LastModifiedDate
public class GameBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(GameBackendApplication.class, args);
	}
}
