package com.chatbot.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@org.springframework.context.annotation.Bean
	public org.springframework.boot.CommandLineRunner alterTable(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				jdbcTemplate.execute("ALTER TABLE travel_plans ADD COLUMN transit_fare INT DEFAULT 0 AFTER total_distance");
				jdbcTemplate.execute("ALTER TABLE travel_plans ADD COLUMN taxi_fare INT DEFAULT 0 AFTER transit_fare");
				System.out.println("========== Database altered successfully! ==========");
			} catch (Exception e) {
				System.out.println("========== Columns already exist or error: " + e.getMessage() + " ==========");
			}
			try {
				jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS notices (" +
						"id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
						"title VARCHAR(255) NOT NULL, " +
						"content TEXT NOT NULL, " +
						"author_id VARCHAR(50) NOT NULL, " +
						"author_name VARCHAR(50) NOT NULL, " +
						"is_pinned BOOLEAN DEFAULT FALSE, " +
						"created_at DATETIME NOT NULL, " +
						"updated_at DATETIME)");
				System.out.println("========== Notices table verified! ==========");
			} catch (Exception e) {
				System.out.println("========== Notices table creation error: " + e.getMessage() + " ==========");
			}
			try {
				jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS posts (" +
						"id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
						"title VARCHAR(255) NOT NULL, " +
						"content TEXT NOT NULL, " +
						"author_id VARCHAR(50) NOT NULL, " +
						"author_name VARCHAR(50) NOT NULL, " +
						"view_count INT DEFAULT 0, " +
						"route_data TEXT, " +
						"route_name VARCHAR(255), " +
						"created_at DATETIME NOT NULL, " +
						"updated_at DATETIME)");
				System.out.println("========== Posts table verified! ==========");
			} catch (Exception e) {
				System.out.println("========== Posts table creation error: " + e.getMessage() + " ==========");
			}
		};
	}

}
