package com.capstone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootApplication
public class CapstoneApplication {

    @Autowired
    private DataSource dataSource;

    public static void main(String[] args) {
        SpringApplication.run(CapstoneApplication.class, args);
    }

    @PostConstruct
    public void checkHealth() {
        System.out.println("\n✨ Application Health Check Initiated! ✨");

        // 🚀 Database Connection Check
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) { // Check if connection is valid within 1 second
                System.out.println("✅ Database Connection: OK! 🥳");
            } else {
                System.out.println("❌ Database Connection: Invalid! 😟");
            }
        } catch (SQLException e) {
            System.out.println("🚨 Database Connection Error: " + e.getMessage() + " 😱");
        }

        // 🧠 System Memory Check
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024); // MB
        long freeMemory = runtime.freeMemory() / (1024 * 1024);   // MB
        long usedMemory = totalMemory - freeMemory;

        System.out.println("📊 Memory Usage: Total " + totalMemory + "MB, Used " + usedMemory + "MB, Free " + freeMemory + "MB 📈");

        System.out.println("💖 Application Health Check Complete! 💖\n");
    }
}
